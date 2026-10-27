package kh.edu.roomrental.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(UUID id, String email, String fullName, String phone,
                               String role, boolean enabled, Instant createdAt) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getFullName(),
                    user.getPhone(), user.getRole().name(), user.isEnabled(), user.getCreatedAt());
        }
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 120) String fullName,
            @Pattern(regexp = "^$|^[+0-9 ()-]{7,30}$", message = "phone format is invalid")
            String phone
    ) {
    }
}
