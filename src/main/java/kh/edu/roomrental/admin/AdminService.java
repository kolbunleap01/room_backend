package kh.edu.roomrental.admin;

import kh.edu.roomrental.common.audit.AuditLog;
import kh.edu.roomrental.common.audit.AuditLogRepository;
import kh.edu.roomrental.common.audit.AuditService;
import kh.edu.roomrental.common.exception.NotFoundException;
import kh.edu.roomrental.common.exception.ConflictException;
import kh.edu.roomrental.common.security.SecurityEvent;
import kh.edu.roomrental.common.security.SecurityEventRepository;
import kh.edu.roomrental.room.Room;
import kh.edu.roomrental.room.RoomDtos;
import kh.edu.roomrental.room.RoomRepository;
import kh.edu.roomrental.room.RoomService;
import kh.edu.roomrental.room.RoomStatus;
import kh.edu.roomrental.rentalrequest.RentalRequestDtos;
import kh.edu.roomrental.rentalrequest.RentalRequestService;
import kh.edu.roomrental.rentalrequest.RentalRequestRepository;
import kh.edu.roomrental.user.User;
import kh.edu.roomrental.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final RoomService roomService;
    private final RentalRequestService rentalRequestService;
    private final SecurityEventRepository securityEventRepository;
    private final RentalRequestRepository rentalRequestRepository;

    public AdminService(UserRepository userRepository, RoomRepository roomRepository,
                        AuditLogRepository auditLogRepository, AuditService auditService,
                        RoomService roomService, RentalRequestService rentalRequestService,
                        SecurityEventRepository securityEventRepository,
                        RentalRequestRepository rentalRequestRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
        this.roomService = roomService;
        this.rentalRequestService = rentalRequestService;
        this.securityEventRepository = securityEventRepository;
        this.rentalRequestRepository = rentalRequestRepository;
    }

    @Transactional(readOnly = true)
    public AdminDtos.DashboardResponse dashboard() {
        return new AdminDtos.DashboardResponse(userRepository.count(), roomRepository.count(),
                roomRepository.countByStatus(RoomStatus.ACTIVE), rentalRequestRepository.count(), auditLogRepository.count());
    }

    @Transactional(readOnly = true)
    public Page<AdminDtos.UserSummary> users(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> new AdminDtos.UserSummary(user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().name(), user.isEnabled(), user.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public Page<AdminDtos.AuditResponse> audits(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(log -> new AdminDtos.AuditResponse(log.getId(), log.getActorUserId(), log.getEventType(),
                        log.getIpAddress(), log.getDetails(), log.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public Page<AdminDtos.SecurityEventResponse> securityEvents(Pageable pageable) {
        return securityEventRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toSecurityEventResponse);
    }

    @Transactional(readOnly = true)
    public Page<RentalRequestDtos.Response> rentalRequests(Pageable pageable) {
        return rentalRequestService.all(pageable);
    }

    @Transactional
    public AdminDtos.UserSummary setUserEnabled(UUID adminId, UUID userId, boolean enabled, String ipAddress) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (adminId.equals(userId) && !enabled) {
            throw new ConflictException("Administrators cannot deactivate their own account");
        }
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        auditService.record(adminId, enabled ? "ADMIN_USER_ACTIVATED" : "ADMIN_USER_DEACTIVATED", ipAddress,
                userId.toString());
        return toUserSummary(saved);
    }

    @Transactional
    public RoomDtos.RoomResponse updateRoom(UUID adminId, UUID roomId, RoomDtos.UpdateRoomRequest request,
                                            String ipAddress) {
        return roomService.update(adminId, roomId, request, ipAddress);
    }

    @Transactional(readOnly = true)
    public Page<RoomDtos.RoomSummary> rooms(RoomStatus status, Pageable pageable) {
        Page<Room> rooms = status == null ? roomRepository.findAll(pageable) : roomRepository.search(status, null, null, null, null, pageable);
        return rooms.map(room -> new RoomDtos.RoomSummary(room.getId(), room.getOwnerId(), room.getTitle(), room.getRoomType(), room.getCity(),
                room.getMonthlyRent(), room.getBedrooms(), room.getBathrooms(), room.isFurnished(), room.getStatus(), room.getCreatedAt()));
    }

    @Transactional
    public void deactivateRoom(UUID adminId, UUID roomId, String ipAddress) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
        room.deactivate();
        roomRepository.save(room);
        auditService.record(adminId, "ADMIN_ROOM_DEACTIVATED", ipAddress, roomId.toString());
    }

    @Transactional
    public void deleteRoom(UUID adminId, UUID roomId, String ipAddress) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
        rentalRequestRepository.deleteByRoomId(roomId);
        roomRepository.delete(room);
        auditService.record(adminId, "ADMIN_ROOM_DELETED", ipAddress, roomId.toString());
    }

    @Transactional
    public RentalRequestDtos.Response decideRentalRequest(UUID adminId, UUID requestId, boolean approve,
                                                          RentalRequestDtos.DecisionRequest request,
                                                          String ipAddress) {
        return rentalRequestService.decide(adminId, requestId, approve, request, ipAddress);
    }

    @Transactional
    public void cancelRentalRequest(UUID adminId, UUID requestId, String ipAddress) {
        rentalRequestService.cancel(adminId, requestId, ipAddress);
    }

    private AdminDtos.UserSummary toUserSummary(User user) {
        return new AdminDtos.UserSummary(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name(),
                user.isEnabled(), user.getCreatedAt());
    }

    private AdminDtos.SecurityEventResponse toSecurityEventResponse(SecurityEvent event) {
        return new AdminDtos.SecurityEventResponse(event.getId(), event.getActorUserId(), event.getEventType(),
                event.getIpAddress(), event.getStatus().name(), event.getDetails(), event.getCreatedAt());
    }
}
