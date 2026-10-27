package kh.edu.roomrental.rentalrequest;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class RentalRequestDtos {
    private RentalRequestDtos() {
    }

    public record CreateRequest(@NotNull @FutureOrPresent LocalDate moveInDate,
                                @Size(max = 2000) String message) {
    }

    public record DecisionRequest(@Size(max = 2000) String note) {
    }

    public record AliasCreateRequest(
            @JsonProperty("room_id") @NotNull UUID roomId,
            @JsonProperty("move_in_date") @FutureOrPresent LocalDate moveInDate,
            @Size(max = 2000) String message) {
    }

    public record StatusUpdateRequest(@NotBlank String status, @Size(max = 2000) String note) {
    }

    public record Response(UUID id, UUID roomId, UUID applicantId, UUID ownerId, LocalDate moveInDate,
                           String message, RentalRequestStatus status, String decisionNote,
                           Instant createdAt, Instant updatedAt) {
        public static Response from(RentalRequest request) {
            return new Response(request.getId(), request.getRoomId(), request.getApplicantId(), request.getOwnerId(),
                    request.getMoveInDate(), request.getMessage(), request.getStatus(), request.getDecisionNote(),
                    request.getCreatedAt(), request.getUpdatedAt());
        }
    }
}
