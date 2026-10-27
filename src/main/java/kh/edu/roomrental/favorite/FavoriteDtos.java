package kh.edu.roomrental.favorite;

import com.fasterxml.jackson.annotation.JsonProperty;
import kh.edu.roomrental.room.RoomDtos;

import java.time.Instant;
import java.util.UUID;

public final class FavoriteDtos {
    private FavoriteDtos() {
    }

    public record FavoriteResponse(UUID roomId, Instant createdAt, RoomDtos.RoomSummary room) {
        @JsonProperty("id")
        public UUID id() {
            return roomId;
        }

        @JsonProperty("title")
        public String title() {
            return room.title();
        }

        @JsonProperty("location")
        public String location() {
            return room.location();
        }

        @JsonProperty("price")
        public java.math.BigDecimal price() {
            return room.price();
        }

        @JsonProperty("description")
        public String description() {
            return "";
        }

        @JsonProperty("owner_id")
        public UUID ownerId() {
            return room.ownerId();
        }
    }
}
