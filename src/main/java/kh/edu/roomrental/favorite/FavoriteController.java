package kh.edu.roomrental.favorite;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.roomrental.user.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/me/favorites", "/api/v1/favorites"})
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public Page<FavoriteDtos.FavoriteResponse> list(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                                    Pageable pageable) {
        return favoriteService.list(principal.getId(), pageable);
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<FavoriteDtos.FavoriteResponse> add(@AuthenticationPrincipal UserPrincipal principal,
                                                              @PathVariable UUID roomId,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(201).body(favoriteService.add(principal.getId(), roomId, request.getRemoteAddr()));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable UUID roomId,
                                       HttpServletRequest request) {
        favoriteService.remove(principal.getId(), roomId, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
