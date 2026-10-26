package kh.edu.roomrental.controller;

import kh.edu.roomrental.api.ApiDtos.RoomRequest;
import kh.edu.roomrental.api.ApiDtos.RoomResponse;
import kh.edu.roomrental.model.RoomListing;
import kh.edu.roomrental.model.UserAccount;
import kh.edu.roomrental.repository.RoomRepository;
import kh.edu.roomrental.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomRepository rooms;
    private final UserRepository users;

    public RoomController(RoomRepository rooms, UserRepository users) {
        this.rooms = rooms;
        this.users = users;
    }

    @GetMapping
    public List<RoomResponse> list(@RequestParam(required = false) String q) {
        return rooms.findByAvailableTrueOrderByIdDesc().stream()
                .filter(r -> q == null || q.isBlank() ||
                        (r.getTitle() + " " + r.getLocation() + " " + r.getRoomType()).toLowerCase().contains(q.toLowerCase()))
                .map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable Long id) { return toResponse(findRoom(id)); }

    @GetMapping("/mine")
    public List<RoomResponse> mine(Authentication auth) {
        return rooms.findByOwnerEmailIgnoreCaseOrderByIdDesc(auth.getName()).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest input, Authentication auth) {
        UserAccount owner = users.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        RoomListing room = new RoomListing();
        apply(input, room);
        room.setOwner(owner);
        return toResponse(rooms.save(room));
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest input, Authentication auth) {
        RoomListing room = findRoom(id);
        requireOwnerOrAdmin(room, auth);
        apply(input, room);
        return toResponse(rooms.save(room));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        RoomListing room = findRoom(id);
        requireOwnerOrAdmin(room, auth);
        rooms.delete(room);
    }

    private void apply(RoomRequest input, RoomListing room) {
        room.setTitle(input.title().trim());
        room.setDescription(input.description().trim());
        room.setPricePerMonth(input.pricePerMonth());
        room.setLocation(input.location().trim());
        room.setRoomType(input.roomType());
        room.setImageUrl(input.imageUrl());
        room.setAvailable(input.available());
    }

    private void requireOwnerOrAdmin(RoomListing room, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
        if (!admin && !room.getOwner().getEmail().equalsIgnoreCase(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own listings");
        }
    }

    private RoomListing findRoom(Long id) {
        return rooms.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    private RoomResponse toResponse(RoomListing r) {
        return new RoomResponse(r.getId(), r.getOwner().getId(), r.getOwner().getFullName(), r.getTitle(), r.getDescription(),
                r.getPricePerMonth(), r.getLocation(), r.getRoomType(), r.getImageUrl(), r.isAvailable(), r.getCreatedAt());
    }
}
