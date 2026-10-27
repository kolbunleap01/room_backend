package kh.edu.roomrental.controller;

import kh.edu.roomrental.api.ApiDtos.CreateRentalRequest;
import kh.edu.roomrental.api.ApiDtos.RentalRequestResponse;
import kh.edu.roomrental.api.ApiDtos.UpdateRequestStatus;
import kh.edu.roomrental.model.*;
import kh.edu.roomrental.repository.RentalRequestRepository;
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
@RequestMapping("/api/requests")
public class RentalRequestController {
    private final RentalRequestRepository requests;
    private final RoomRepository rooms;
    private final UserRepository users;

    public RentalRequestController(RentalRequestRepository requests, RoomRepository rooms, UserRepository users) {
        this.requests = requests;
        this.rooms = rooms;
        this.users = users;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalRequestResponse create(@Valid @RequestBody CreateRentalRequest input, Authentication auth) {
        RoomListing room = rooms.findById(input.roomId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        UserAccount requester = currentUser(auth);
        if (!room.isAvailable()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available");
        if (room.getOwner().getId().equals(requester.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot request your own room");
        RentalRequest request = new RentalRequest();
        request.setRoom(room);
        request.setRequester(requester);
        request.setMessage(input.message().trim());
        request.setStatus(RequestStatus.PENDING);
        return toResponse(requests.save(request));
    }

    @GetMapping("/outbox")
    public List<RentalRequestResponse> outbox(Authentication auth) {
        return requests.findByRequesterEmailIgnoreCaseOrderByCreatedAtDesc(auth.getName()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/inbox")
    public List<RentalRequestResponse> inbox(Authentication auth) {
        if (isAdmin(auth)) return requests.findAll().stream().map(this::toResponse).toList();
        return requests.findByRoomOwnerEmailIgnoreCaseOrderByCreatedAtDesc(auth.getName()).stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/status")
    public RentalRequestResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateRequestStatus input, Authentication auth) {
        RentalRequest request = requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        boolean admin = isAdmin(auth);
        boolean owner = request.getRoom().getOwner().getEmail().equalsIgnoreCase(auth.getName());
        boolean requesterCancelling = request.getRequester().getEmail().equalsIgnoreCase(auth.getName()) && input.status() == RequestStatus.CANCELLED;
        if (!admin && !owner && !requesterCancelling) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (request.getStatus() != RequestStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT, "Request was already decided");
        request.setStatus(input.status());
        if (input.status() == RequestStatus.APPROVED) request.getRoom().setAvailable(false);
        return toResponse(requests.save(request));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
    }

    private UserAccount currentUser(Authentication auth) {
        return users.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private RentalRequestResponse toResponse(RentalRequest r) {
        return new RentalRequestResponse(r.getId(), r.getRoom().getId(), r.getRoom().getTitle(), r.getRequester().getId(),
                r.getRequester().getFullName(), r.getMessage(), r.getStatus(), r.getCreatedAt());
    }
}
