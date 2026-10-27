package kh.edu.roomrental.rentalrequest;

import kh.edu.roomrental.common.audit.AuditService;
import kh.edu.roomrental.common.exception.ConflictException;
import kh.edu.roomrental.common.exception.ForbiddenException;
import kh.edu.roomrental.common.exception.NotFoundException;
import kh.edu.roomrental.room.Room;
import kh.edu.roomrental.room.RoomRepository;
import kh.edu.roomrental.room.RoomStatus;
import kh.edu.roomrental.user.Role;
import kh.edu.roomrental.user.User;
import kh.edu.roomrental.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RentalRequestService {
    private final RentalRequestRepository rentalRequestRepository;
    private final RoomRepository roomRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public RentalRequestService(RentalRequestRepository rentalRequestRepository,
                                 RoomRepository roomRepository,
                                 AuditService auditService,
                                 UserRepository userRepository) {
        this.rentalRequestRepository = rentalRequestRepository;
        this.roomRepository = roomRepository;
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    @Transactional
    public RentalRequestDtos.Response create(UUID applicantId, UUID roomId,
                                             RentalRequestDtos.CreateRequest request, String ipAddress) {
        Room room = roomRepository.findById(roomId)
                .filter(candidate -> candidate.getStatus() == RoomStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        if (room.getOwnerId().equals(applicantId)) {
            throw new ConflictException("Room owners cannot request their own room");
        }
        if (rentalRequestRepository.existsByRoomIdAndApplicantIdAndStatus(roomId, applicantId, RentalRequestStatus.PENDING)) {
            throw new ConflictException("A pending request already exists for this room");
        }
        RentalRequest rentalRequest = rentalRequestRepository.save(
                new RentalRequest(roomId, applicantId, room.getOwnerId(), request));
        auditService.record(applicantId, "RENTAL_REQUEST_CREATED", ipAddress, rentalRequest.getId().toString());
        return RentalRequestDtos.Response.from(rentalRequest);
    }

    @Transactional(readOnly = true)
    public Page<RentalRequestDtos.Response> mine(UUID applicantId, Pageable pageable) {
        return rentalRequestRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId, pageable)
                .map(RentalRequestDtos.Response::from);
    }

    @Transactional(readOnly = true)
    public Page<RentalRequestDtos.Response> forOwner(UUID ownerId, Pageable pageable) {
        return rentalRequestRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId, pageable)
                .map(RentalRequestDtos.Response::from);
    }

    @Transactional(readOnly = true)
    public Page<RentalRequestDtos.Response> all(Pageable pageable) {
        return rentalRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(RentalRequestDtos.Response::from);
    }

    @Transactional
    public RentalRequestDtos.Response decide(UUID ownerId, UUID requestId, boolean approve,
                                              RentalRequestDtos.DecisionRequest request, String ipAddress) {
        RentalRequest rentalRequest = getRequiredForUpdate(requestId);
        if (!rentalRequest.getOwnerId().equals(ownerId) && !isAdmin(ownerId)) {
            throw new ForbiddenException("Only the room owner or an administrator can decide this request");
        }
        if (rentalRequest.getStatus() != RentalRequestStatus.PENDING) {
            throw new ConflictException("Only pending requests can be decided");
        }
        if (approve) {
            Room room = roomRepository.findByIdForUpdate(rentalRequest.getRoomId())
                    .orElseThrow(() -> new NotFoundException("Room not found"));
            if (room.getStatus() != RoomStatus.ACTIVE) {
                throw new ConflictException("Cannot approve a rental request for an inactive room");
            }
            room.deactivate();
            roomRepository.save(room);
            rentalRequest.approve(request == null ? null : request.note());
        } else {
            rentalRequest.reject(request == null ? null : request.note());
        }
        auditService.record(ownerId, isAdmin(ownerId)
                        ? (approve ? "ADMIN_RENTAL_REQUEST_APPROVED" : "ADMIN_RENTAL_REQUEST_REJECTED")
                        : (approve ? "RENTAL_REQUEST_APPROVED" : "RENTAL_REQUEST_REJECTED"),
                ipAddress, requestId.toString());
        return RentalRequestDtos.Response.from(rentalRequestRepository.save(rentalRequest));
    }

    @Transactional
    public RentalRequestDtos.Response cancel(UUID applicantId, UUID requestId, String ipAddress) {
        RentalRequest rentalRequest = getRequiredForUpdate(requestId);
        boolean admin = isAdmin(applicantId);
        if (!rentalRequest.getApplicantId().equals(applicantId) && !admin) {
            throw new ForbiddenException("Only the applicant or an administrator can cancel this request");
        }
        if (rentalRequest.getStatus() != RentalRequestStatus.PENDING) {
            throw new ConflictException("Only pending requests can be cancelled");
        }
        rentalRequest.cancel();
        rentalRequestRepository.save(rentalRequest);
        auditService.record(applicantId, admin ? "ADMIN_RENTAL_REQUEST_CANCELLED" : "RENTAL_REQUEST_CANCELLED",
                ipAddress, requestId.toString());
        return RentalRequestDtos.Response.from(rentalRequest);
    }

    private boolean isAdmin(UUID userId) {
        return userRepository.findById(userId).map(User::getRole).orElse(Role.USER) == Role.ADMIN;
    }

    private RentalRequest getRequired(UUID requestId) {
        return rentalRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Rental request not found"));
    }

    private RentalRequest getRequiredForUpdate(UUID requestId) {
        return rentalRequestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new NotFoundException("Rental request not found"));
    }
}
