package kh.edu.roomrental.user;

import kh.edu.roomrental.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getRequired(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public UserDtos.UserResponse updateProfile(UUID userId, UserDtos.UpdateProfileRequest request) {
        User user = getRequired(userId);
        user.updateProfile(request.fullName().trim(), normalizePhone(request.phone()));
        return UserDtos.UserResponse.from(userRepository.save(user));
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }
}
