package kh.edu.roomrental.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 12, max = 72) String password,
            @NotBlank @Size(max = 120) @JsonAlias({"name", "full_name"}) String fullName,
            @Size(max = 30) String phone
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(max = 72) String password
    ) {
    }

    public record RefreshRequest(@NotBlank @Size(max = 512) String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType,
                                long expiresInSeconds, UUID id, String email, String fullName,
                                String phone, String role, boolean enabled) {
        public TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
            this(accessToken, refreshToken, tokenType, expiresInSeconds, null, null, null, null, null, true);
        }
    }
}
