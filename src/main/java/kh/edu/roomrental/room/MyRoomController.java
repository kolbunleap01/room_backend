package kh.edu.roomrental.room;

import kh.edu.roomrental.user.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/me/rooms", "/api/v1/rooms/mine"})
public class MyRoomController {
    private final RoomService roomService;

    public MyRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Page<RoomDtos.RoomSummary> mine(@AuthenticationPrincipal UserPrincipal principal,
                                           @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                           Pageable pageable) {
        return roomService.findMine(principal.getId(), pageable);
    }
}
