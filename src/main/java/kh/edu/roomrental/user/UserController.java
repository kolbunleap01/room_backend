package kh.edu.roomrental.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/me", "/api/v1/users/me"})
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserDtos.UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserDtos.UserResponse.from(userService.getRequired(principal.getId()));
    }

    @PatchMapping
    public ResponseEntity<UserDtos.UserResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), request));
    }
}
