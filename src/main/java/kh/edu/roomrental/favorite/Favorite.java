package kh.edu.roomrental.favorite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
public class Favorite {
    @jakarta.persistence.Id
    @Column(name = "user_id")
    private UUID userId;

    @jakarta.persistence.Id
    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Favorite() {
    }

    public Favorite(UUID userId, UUID roomId) {
        this.userId = userId;
        this.roomId = roomId;
        this.createdAt = Instant.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
