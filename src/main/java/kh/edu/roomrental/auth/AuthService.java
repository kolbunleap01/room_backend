package kh.edu.roomrental.auth;

import kh.edu.roomrental.common.audit.AuditService;
import kh.edu.roomrental.common.exception.ConflictException;
import kh.edu.roomrental.common.exception.UnauthorizedException;
import kh.edu.roomrental.user.User;
import kh.edu.roomrental.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DUMMY_PASSWORD_HASH = "$2a$12$7EqJtq98hPqEX7fNZaFWoO3hC4Qn7e2jTzjv3vR9B6vP0v8uJ4WQe";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final LoginAttemptService loginAttemptService;
    private final Duration refreshTtl;
    private final int maxFailedAttempts;
    private final Duration lockDuration;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditService auditService,
                       LoginAttemptService loginAttemptService,
                       @Value("${app.jwt.refresh-ttl:P30D}") Duration refreshTtl,
                       @Value("${app.login.max-failed-attempts:5}") int maxFailedAttempts,
                       @Value("${app.login.lock-duration:PT15M}") Duration lockDuration) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.loginAttemptService = loginAttemptService;
        this.refreshTtl = refreshTtl;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockDuration = lockDuration;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest request, String ipAddress) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with these credentials already exists");
        }
        User user = new User(email, passwordEncoder.encode(request.password()), request.fullName().trim(),
                normalizePhone(request.phone()));
        userRepository.save(user);
        auditService.record(user.getId(), "USER_REGISTERED", ipAddress, "account created");
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request, String ipAddress) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        Instant now = Instant.now();
        if (user == null) {
            passwordEncoder.matches(request.password(), DUMMY_PASSWORD_HASH);
            loginAttemptService.record(null, email, ipAddress, LoginAttemptStatus.FAILED, "invalid credentials");
            auditService.record(null, "LOGIN_FAILED", ipAddress, "invalid credentials");
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!user.isEnabled()) {
            loginAttemptService.record(user.getId(), email, ipAddress, LoginAttemptStatus.BLOCKED,
                    "account disabled");
            auditService.record(user.getId(), "LOGIN_BLOCKED", ipAddress, "account disabled");
            throw new UnauthorizedException("Invalid credentials");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            loginAttemptService.record(user.getId(), email, ipAddress, LoginAttemptStatus.BLOCKED,
                    "account temporarily locked");
            auditService.record(user.getId(), "LOGIN_BLOCKED", ipAddress, "account temporarily locked");
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int nextAttempts = user.getFailedLoginAttempts() + 1;
            user.recordFailedLogin(nextAttempts >= maxFailedAttempts ? now.plus(lockDuration) : null);
            userRepository.save(user);
            loginAttemptService.record(user.getId(), email, ipAddress,
                    nextAttempts >= maxFailedAttempts ? LoginAttemptStatus.BLOCKED : LoginAttemptStatus.FAILED,
                    "invalid credentials");
            auditService.record(user.getId(), "LOGIN_FAILED", ipAddress, "invalid credentials");
            throw new UnauthorizedException("Invalid credentials");
        }
        user.resetLoginAttempts();
        userRepository.save(user);
        loginAttemptService.record(user.getId(), email, ipAddress, LoginAttemptStatus.SUCCESS,
                "password authentication");
        auditService.record(user.getId(), "LOGIN_SUCCEEDED", ipAddress, "password authentication");
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthDtos.TokenResponse refresh(String rawRefreshToken, String ipAddress) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!current.isUsable(Instant.now())) {
            auditService.record(current.getUserId(), "REFRESH_REUSE_OR_EXPIRED", ipAddress, "refresh token rejected");
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = userRepository.findById(current.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!user.isEnabled() || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now()))) {
            current.rotate(null);
            refreshTokenRepository.save(current);
            auditService.record(user.getId(), "REFRESH_BLOCKED", ipAddress, "account unavailable");
            throw new UnauthorizedException("Invalid refresh token");
        }
        String rawReplacement = generateRefreshToken();
        RefreshToken replacement = new RefreshToken(user.getId(), hash(rawReplacement), Instant.now().plus(refreshTtl));
        current.rotate(replacement.getId());
        refreshTokenRepository.save(current);
        refreshTokenRepository.save(replacement);
        auditService.record(user.getId(), "REFRESH_ROTATED", ipAddress, "refresh token rotated");
        return new AuthDtos.TokenResponse(jwtService.issueAccessToken(user), rawReplacement, "Bearer",
                jwtService.getAccessTtl().toSeconds(), user.getId(), user.getEmail(), user.getFullName(),
                user.getPhone(), user.getRole().name(), user.isEnabled());
    }

    @Transactional
    public void logout(String rawRefreshToken, String ipAddress) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken)).ifPresent(token -> {
            if (token.isUsable(Instant.now())) {
                token.rotate(null);
                refreshTokenRepository.save(token);
                auditService.record(token.getUserId(), "LOGOUT", ipAddress, "refresh token revoked");
            }
        });
    }

    private AuthDtos.TokenResponse issueTokens(User user) {
        String rawRefreshToken = generateRefreshToken();
        refreshTokenRepository.save(new RefreshToken(user.getId(), hash(rawRefreshToken), Instant.now().plus(refreshTtl)));
        return new AuthDtos.TokenResponse(jwtService.issueAccessToken(user), rawRefreshToken, "Bearer",
                jwtService.getAccessTtl().toSeconds(), user.getId(), user.getEmail(), user.getFullName(),
                user.getPhone(), user.getRole().name(), user.isEnabled());
    }

    private String generateRefreshToken() {
        byte[] token = new byte[48];
        SECURE_RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
