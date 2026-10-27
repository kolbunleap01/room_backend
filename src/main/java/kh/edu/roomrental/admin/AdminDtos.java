package kh.edu.roomrental.admin;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record DashboardResponse(long users, long rooms, long activeRooms, long requests, long auditEvents) {
    }

    public record UserSummary(UUID id, String email, String fullName, String role, boolean enabled, Instant createdAt) {
    }

    public record AuditResponse(UUID id, UUID actorUserId, String eventType, String ipAddress,
                                String details, Instant createdAt) {
    }

    public record UserStatusRequest(@NotNull Boolean enabled) {
    }

    public record SecurityEventResponse(UUID id, UUID actorUserId, String eventType, String ipAddress,
                                        String status, String details, Instant createdAt) {
    }
}
