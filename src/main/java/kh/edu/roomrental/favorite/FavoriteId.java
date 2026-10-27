package kh.edu.roomrental.favorite;

import java.io.Serializable;
import java.util.UUID;

public class FavoriteId implements Serializable {
    private UUID userId;
    private UUID roomId;

    public FavoriteId() {
    }

    public FavoriteId(UUID userId, UUID roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FavoriteId that)) return false;
        return userId.equals(that.userId) && roomId.equals(that.roomId);
    }

    @Override
    public int hashCode() {
        return 31 * userId.hashCode() + roomId.hashCode();
    }
}
