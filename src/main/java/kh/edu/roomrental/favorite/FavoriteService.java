package kh.edu.roomrental.favorite;

import kh.edu.roomrental.common.audit.AuditService;
import kh.edu.roomrental.common.exception.ConflictException;
import kh.edu.roomrental.common.exception.NotFoundException;
import kh.edu.roomrental.room.Room;
import kh.edu.roomrental.room.RoomDtos;
import kh.edu.roomrental.room.RoomRepository;
import kh.edu.roomrental.room.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final RoomRepository roomRepository;
    private final AuditService auditService;

    public FavoriteService(FavoriteRepository favoriteRepository, RoomRepository roomRepository, AuditService auditService) {
        this.favoriteRepository = favoriteRepository;
        this.roomRepository = roomRepository;
        this.auditService = auditService;
    }

    @Transactional
    public FavoriteDtos.FavoriteResponse add(UUID userId, UUID roomId, String ipAddress) {
        Room room = roomRepository.findById(roomId)
                .filter(candidate -> candidate.getStatus() == RoomStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        FavoriteId id = new FavoriteId(userId, roomId);
        if (favoriteRepository.existsById(id)) {
            throw new ConflictException("Room is already in favorites");
        }
        Favorite favorite = favoriteRepository.save(new Favorite(userId, roomId));
        auditService.record(userId, "FAVORITE_ADDED", ipAddress, roomId.toString());
        return new FavoriteDtos.FavoriteResponse(roomId, favorite.getCreatedAt(), toSummary(room));
    }

    @Transactional
    public void remove(UUID userId, UUID roomId, String ipAddress) {
        FavoriteId id = new FavoriteId(userId, roomId);
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
            auditService.record(userId, "FAVORITE_REMOVED", ipAddress, roomId.toString());
        }
    }

    @Transactional(readOnly = true)
    public Page<FavoriteDtos.FavoriteResponse> list(UUID userId, Pageable pageable) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(favorite -> roomRepository.findById(favorite.getRoomId())
                        .map(room -> new FavoriteDtos.FavoriteResponse(favorite.getRoomId(), favorite.getCreatedAt(), toSummary(room)))
                        .orElseThrow(() -> new NotFoundException("Room not found")));
    }

    private RoomDtos.RoomSummary toSummary(Room room) {
        return new RoomDtos.RoomSummary(room.getId(), room.getOwnerId(), room.getTitle(), room.getRoomType(), room.getCity(),
                room.getMonthlyRent(), room.getBedrooms(), room.getBathrooms(), room.isFurnished(),
                room.getStatus(), room.getCreatedAt());
    }
}
