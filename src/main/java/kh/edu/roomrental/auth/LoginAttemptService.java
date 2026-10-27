package kh.edu.roomrental.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Transactional
    public void record(UUID userId, String email, String ipAddress, LoginAttemptStatus status, String details) {
        loginAttemptRepository.save(new LoginAttempt(userId, trim(email, 320), trim(ipAddress, 64), status,
                trim(details, 500)));
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
