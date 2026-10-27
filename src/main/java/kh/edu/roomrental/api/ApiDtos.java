package kh.edu.roomrental.api;

import jakarta.validation.constraints.*;
import kh.edu.roomrental.model.RequestStatus;
import kh.edu.roomrental.model.Role;

import java.time.Instant;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 180) String email,
            @Size(max = 40) String phone,
            @NotBlank @Size(min = 8, max = 100) String password) {}

    public record UserResponse(Long id, String fullName, String email, String phone, Role role) {}

    public record RoomRequest(
            @NotBlank @Size(max = 120) String title,
            @NotBlank @Size(max = 1000) String description,
            @Positive long pricePerMonth,
            @NotBlank @Size(max = 240) String location,
            @Size(max = 80) String roomType,
            @Size(max = 500) String imageUrl,
            boolean available) {}

    public record RoomResponse(
            Long id, Long ownerId, String ownerName, String title, String description,
            long pricePerMonth, String location, String roomType, String imageUrl,
            boolean available, Instant createdAt) {}

    public record CreateRentalRequest(
            @NotNull Long roomId,
            @NotBlank @Size(max = 1000) String message) {}

    public record UpdateRequestStatus(@NotNull RequestStatus status) {}

    public record RentalRequestResponse(
            Long id, Long roomId, String roomTitle, Long requesterId, String requesterName,
            String message, RequestStatus status, Instant createdAt) {}

    public record AdminSummary(long users, long rooms, long pendingRequests) {}
}
