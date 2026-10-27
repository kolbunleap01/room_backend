package kh.edu.roomrental.room;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kh.edu.roomrental.user.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Page<RoomDtos.RoomSummary> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minRent,
            @RequestParam(required = false, name = "maxRent") BigDecimal maxRent,
            @RequestParam(required = false, name = "max_price") BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return roomService.search(query != null ? query : search, city != null ? city : location, minRent,
                maxRent != null ? maxRent : maxPrice, pageable);
    }

    @GetMapping("/{roomId}")
    public RoomDtos.RoomResponse get(@PathVariable UUID roomId) {
        return roomService.getPublic(roomId);
    }

    @PostMapping
    public ResponseEntity<RoomDtos.RoomResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody RoomDtos.CreateRoomRequest request,
                                                        HttpServletRequest httpRequest) {
        return ResponseEntity.status(201).body(roomService.create(principal.getId(), request, httpRequest.getRemoteAddr()));
    }

    @PatchMapping("/{roomId}")
    public RoomDtos.RoomResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable UUID roomId,
                                        @Valid @RequestBody RoomDtos.UpdateRoomRequest request,
                                        HttpServletRequest httpRequest) {
        return roomService.update(principal.getId(), roomId, request, httpRequest.getRemoteAddr());
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable UUID roomId,
                                       HttpServletRequest httpRequest) {
        roomService.delete(principal.getId(), roomId, httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
