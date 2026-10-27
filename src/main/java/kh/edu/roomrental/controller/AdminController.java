package kh.edu.roomrental.controller;

import kh.edu.roomrental.api.ApiDtos.AdminSummary;
import kh.edu.roomrental.api.ApiDtos.RoomResponse;
import kh.edu.roomrental.api.ApiDtos.UserResponse;
import kh.edu.roomrental.model.RequestStatus;
import kh.edu.roomrental.model.RoomListing;
import kh.edu.roomrental.model.UserAccount;
import kh.edu.roomrental.repository.RentalRequestRepository;
import kh.edu.roomrental.repository.RoomRepository;
import kh.edu.roomrental.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository users;
    private final RoomRepository rooms;
    private final RentalRequestRepository requests;

    public AdminController(UserRepository users, RoomRepository rooms, RentalRequestRepository requests) {
        this.users = users;
        this.rooms = rooms;
        this.requests = requests;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return users.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/rooms")
    public List<RoomResponse> rooms() {
        return rooms.findAllByOrderByIdDesc().stream().map(this::toRoomResponse).toList();
    }

    @GetMapping("/summary")
    public AdminSummary summary() {
        return new AdminSummary(users.count(), rooms.count(), requests.countByStatus(RequestStatus.PENDING));
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole());
    }

    private RoomResponse toRoomResponse(RoomListing r) {
        return new RoomResponse(r.getId(), r.getOwner().getId(), r.getOwner().getFullName(), r.getTitle(), r.getDescription(),
                r.getPricePerMonth(), r.getLocation(), r.getRoomType(), r.getImageUrl(), r.isAvailable(), r.getCreatedAt());
    }
}
