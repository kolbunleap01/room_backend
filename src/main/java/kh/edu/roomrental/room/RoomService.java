package kh.edu.roomrental.room;

import kh.edu.roomrental.common.audit.AuditService;
import kh.edu.roomrental.common.exception.ForbiddenException;
import kh.edu.roomrental.common.exception.NotFoundException;
import kh.edu.roomrental.user.User;
import kh.edu.roomrental.user.UserRepository;
import kh.edu.roomrental.user.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository, AuditService auditService) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<RoomDtos.RoomSummary> search(String query, String city, BigDecimal minRent,
                                             BigDecimal maxRent, Pageable pageable) {
        return roomRepository.search(RoomStatus.ACTIVE, normalize(query), normalize(city), minRent, maxRent, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public RoomDtos.RoomResponse getPublic(UUID roomId) {
        Room room = getRequired(roomId);
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new NotFoundException("Room not found");
        }
        return toResponse(room);
    }

    @Transactional
    public RoomDtos.RoomResponse create(UUID ownerId, RoomDtos.CreateRoomRequest request, String ipAddress) {
        Room room = roomRepository.save(new Room(ownerId, request));
        auditService.record(ownerId, "ROOM_CREATED", ipAddress, room.getId().toString());
        return toResponse(room);
    }

    @Transactional
    public RoomDtos.RoomResponse update(UUID actorId, UUID roomId, RoomDtos.UpdateRoomRequest request, String ipAddress) {
        Room room = getRequired(roomId);
        ensureOwnerOrAdmin(actorId, room);
        room.update(request);
        auditService.record(actorId, isAdmin(actorId) ? "ADMIN_ROOM_UPDATED" : "ROOM_UPDATED", ipAddress,
                roomId.toString());
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public void delete(UUID actorId, UUID roomId, String ipAddress) {
        Room room = getRequired(roomId);
        ensureOwnerOrAdmin(actorId, room);
        room.deactivate();
        roomRepository.save(room);
        auditService.record(actorId, isAdmin(actorId) ? "ADMIN_ROOM_DEACTIVATED" : "ROOM_DEACTIVATED", ipAddress,
                roomId.toString());
    }

    @Transactional(readOnly = true)
    public Page<RoomDtos.RoomSummary> findMine(UUID ownerId, Pageable pageable) {
        return roomRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<RoomDtos.RoomSummary> findAllForAdmin(RoomStatus status, Pageable pageable) {
        Page<Room> page = status == null
                ? roomRepository.findAll(pageable)
                : roomRepository.search(status, null, null, null, null, pageable);
        return page.map(this::toSummary);
    }

    public Room getRequired(UUID roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
    }

    public void ensureOwner(UUID actorId, Room room) {
        if (!room.getOwnerId().equals(actorId)) {
            throw new ForbiddenException("Only the room owner can change this room");
        }
    }

    public void ensureOwnerOrAdmin(UUID actorId, Room room) {
        if (room.getOwnerId().equals(actorId)) {
            return;
        }
        if (userRepository.findById(actorId).map(User::getRole).orElse(Role.USER) != Role.ADMIN) {
            throw new ForbiddenException("Only the room owner or an administrator can change this room");
        }
    }

    private boolean isAdmin(UUID actorId) {
        return userRepository.findById(actorId).map(User::getRole).orElse(Role.USER) == Role.ADMIN;
    }

    private RoomDtos.RoomSummary toSummary(Room room) {
        return new RoomDtos.RoomSummary(room.getId(), room.getOwnerId(), room.getTitle(), room.getRoomType(), room.getCity(),
                room.getMonthlyRent(), room.getBedrooms(), room.getBathrooms(), room.isFurnished(),
                room.getStatus(), room.getCreatedAt());
    }

    private RoomDtos.RoomResponse toResponse(Room room) {
        String ownerName = userRepository.findById(room.getOwnerId()).map(User::getFullName).orElse(null);
        return new RoomDtos.RoomResponse(room.getId(), room.getOwnerId(), ownerName, room.getTitle(),
                room.getRoomType(), room.getDescription(), room.getAddress(), room.getCity(), room.getDistrict(), room.getMonthlyRent(),
                room.getDeposit(), room.getBedrooms(), room.getBathrooms(), room.getAreaSqm(), room.isFurnished(),
                room.getAvailableFrom(), room.getStatus(), room.getCreatedAt(), room.getUpdatedAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
