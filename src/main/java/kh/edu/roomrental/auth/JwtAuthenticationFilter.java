package kh.edu.roomrental.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.edu.roomrental.user.UserPrincipal;
import kh.edu.roomrental.user.UserRepository;
import kh.edu.roomrental.common.security.SecurityEventService;
import kh.edu.roomrental.common.security.SecurityEventStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SecurityEventService securityEventService;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository,
                                  SecurityEventService securityEventService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.securityEventService = securityEventService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Claims claims = jwtService.parse(authorization.substring(7));
            UserPrincipal principal = userRepository.findById(jwtService.userId(claims))
                    .map(UserPrincipal::from)
                    .orElse(null);
            if (principal != null && principal.isEnabled()
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            securityEventService.record(null, "INVALID_ACCESS_TOKEN", request.getRemoteAddr(),
                    SecurityEventStatus.FAILURE, request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}
