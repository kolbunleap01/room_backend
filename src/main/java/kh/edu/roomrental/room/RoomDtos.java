package kh.edu.roomrental.room;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class RoomDtos {
    private RoomDtos() {
    }

    public interface RoomFields {
        @NotBlank @Size(max = 160) String title();

        @NotBlank @Size(max = 20)
        @Pattern(regexp = "(?i)(PRIVATE[ _-]+ROOM|SHARED[ _-]+ROOM|STUDIO|APARTMENT)",
                message = "must be PRIVATE_ROOM, SHARED_ROOM, STUDIO, or APARTMENT")
        String roomType();

        @NotBlank @Size(max = 5000) String description();

        @NotBlank @Size(max = 255) String address();

        @NotBlank @Size(max = 100) String city();

        @Size(max = 100) String district();

        @NotNull @DecimalMin(value = "0.01") BigDecimal monthlyRent();

        @DecimalMin(value = "0.00") BigDecimal deposit();

        @Min(0) int bedrooms();

        @Min(1) int bathrooms();

        @DecimalMin(value = "0.01") BigDecimal areaSqm();

        boolean furnished();

        LocalDate availableFrom();
    }

    public record CreateRoomRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 20)
            @Pattern(regexp = "(?i)(PRIVATE[ _-]+ROOM|SHARED[ _-]+ROOM|STUDIO|APARTMENT)",
                    message = "must be PRIVATE_ROOM, SHARED_ROOM, STUDIO, or APARTMENT")
            String roomType,
            @NotBlank @Size(max = 5000) String description,
            @NotBlank @Size(max = 255) @JsonAlias("location") String address,
            @NotBlank @Size(max = 100) String city,
            @Size(max = 100) String district,
            @NotNull @DecimalMin(value = "0.01") @JsonAlias({"price", "monthly_rent"}) BigDecimal monthlyRent,
            @DecimalMin(value = "0.00") BigDecimal deposit,
            @Min(0) int bedrooms,
            @Min(1) int bathrooms,
            @DecimalMin(value = "0.01") BigDecimal areaSqm,
            boolean furnished, LocalDate availableFrom
    ) implements RoomFields {
        public CreateRoomRequest {
            if ((city == null || city.isBlank()) && address != null) {
                city = address;
            }
        }
    }

    public record UpdateRoomRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 20)
            @Pattern(regexp = "(?i)(PRIVATE[ _-]+ROOM|SHARED[ _-]+ROOM|STUDIO|APARTMENT)",
                    message = "must be PRIVATE_ROOM, SHARED_ROOM, STUDIO, or APARTMENT")
            String roomType,
            @NotBlank @Size(max = 5000) String description,
            @NotBlank @Size(max = 255) @JsonAlias("location") String address,
            @NotBlank @Size(max = 100) String city,
            @Size(max = 100) String district,
            @NotNull @DecimalMin(value = "0.01") @JsonAlias({"price", "monthly_rent"}) BigDecimal monthlyRent,
            @DecimalMin(value = "0.00") BigDecimal deposit,
            @Min(0) int bedrooms,
            @Min(1) int bathrooms,
            @DecimalMin(value = "0.01") BigDecimal areaSqm, boolean furnished, LocalDate availableFrom
    ) implements RoomFields {
        public UpdateRoomRequest {
            if ((city == null || city.isBlank()) && address != null) {
                city = address;
            }
        }
    }

    public record RoomResponse(UUID id, UUID ownerId, String ownerName, String title, String roomType, String description,
                               String address, String city, String district, BigDecimal monthlyRent,
                                BigDecimal deposit, int bedrooms, int bathrooms, BigDecimal areaSqm,
                                boolean furnished, LocalDate availableFrom, RoomStatus status,
                                Instant createdAt, Instant updatedAt) {
        @JsonProperty("location")
        public String location() {
            return address;
        }

        @JsonProperty("price")
        public BigDecimal price() {
            return monthlyRent;
        }
    }

    public record RoomSummary(UUID id, UUID ownerId, String title, String roomType, String city, BigDecimal monthlyRent,
                               int bedrooms, int bathrooms, boolean furnished, RoomStatus status,
                               Instant createdAt) {
        @JsonProperty("location")
        public String location() {
            return city;
        }

        @JsonProperty("price")
        public BigDecimal price() {
            return monthlyRent;
        }
    }
}
