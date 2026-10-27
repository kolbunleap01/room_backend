package kh.edu.roomrental.rentalrequest;

import kh.edu.roomrental.common.audit.AuditService;
import kh.edu.roomrental.room.Room;
import kh.edu.roomrental.room.RoomDtos;
import kh.edu.roomrental.room.RoomRepository;
import kh.edu.roomrental.room.RoomStatus;
import kh.edu.roomrental.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RentalRequestServiceTest {
    @Test
    void approvingPendingRequestDeactivatesRoom() {
        UUID ownerId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        Room room = room(ownerId);
        RentalRequest request = request(room.getId(), applicantId, ownerId);
        RentalRequestRepository requestRepository = mock(RentalRequestRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(roomRepository.findByIdForUpdate(room.getId())).thenReturn(Optional.of(room));
        when(requestRepository.save(any(RentalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalRequestService service = service(requestRepository, roomRepository);

        service.decide(ownerId, request.getId(), true, new RentalRequestDtos.DecisionRequest(null), "127.0.0.1");

        assertEquals(RentalRequestStatus.APPROVED, request.getStatus());
        assertEquals(RoomStatus.INACTIVE, room.getStatus());
    }

    @Test
    void rejectingPendingRequestLeavesRoomActive() {
        UUID ownerId = UUID.randomUUID();
        Room room = room(ownerId);
        RentalRequest request = request(room.getId(), UUID.randomUUID(), ownerId);
        RentalRequestRepository requestRepository = mock(RentalRequestRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(requestRepository.save(any(RentalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalRequestService service = service(requestRepository, roomRepository);

        service.decide(ownerId, request.getId(), false, new RentalRequestDtos.DecisionRequest(null), "127.0.0.1");

        assertEquals(RentalRequestStatus.REJECTED, request.getStatus());
        assertEquals(RoomStatus.ACTIVE, room.getStatus());
    }

    @Test
    void approvingRequestForInactiveRoomIsRejected() {
        UUID ownerId = UUID.randomUUID();
        Room room = room(ownerId);
        room.deactivate();
        RentalRequest request = request(room.getId(), UUID.randomUUID(), ownerId);
        RentalRequestRepository requestRepository = mock(RentalRequestRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(roomRepository.findByIdForUpdate(room.getId())).thenReturn(Optional.of(room));

        RentalRequestService service = service(requestRepository, roomRepository);

        assertThrows(kh.edu.roomrental.common.exception.ConflictException.class,
                () -> service.decide(ownerId, request.getId(), true,
                        new RentalRequestDtos.DecisionRequest(null), "127.0.0.1"));

        assertEquals(RentalRequestStatus.PENDING, request.getStatus());
        assertEquals(RoomStatus.INACTIVE, room.getStatus());
    }

    @Test
    void cancellingPendingRequestLeavesRoomActive() {
        UUID ownerId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        Room room = room(ownerId);
        RentalRequest request = request(room.getId(), applicantId, ownerId);
        RentalRequestRepository requestRepository = mock(RentalRequestRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(requestRepository.save(any(RentalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalRequestService service = service(requestRepository, roomRepository);

        service.cancel(applicantId, request.getId(), "127.0.0.1");

        assertEquals(RentalRequestStatus.CANCELLED, request.getStatus());
        assertEquals(RoomStatus.ACTIVE, room.getStatus());
    }

    private RentalRequestService service(RentalRequestRepository requestRepository, RoomRepository roomRepository) {
        return new RentalRequestService(requestRepository, roomRepository, mock(AuditService.class),
                mock(UserRepository.class));
    }

    private Room room(UUID ownerId) {
        return new Room(ownerId, new RoomDtos.CreateRoomRequest("Room", "STUDIO", "Description", "Address",
                "City", null, BigDecimal.TEN, BigDecimal.ZERO, 1, 1, BigDecimal.ONE, false, LocalDate.now()));
    }

    private RentalRequest request(UUID roomId, UUID applicantId, UUID ownerId) {
        return new RentalRequest(roomId, applicantId, ownerId,
                new RentalRequestDtos.CreateRequest(LocalDate.now(), null));
    }
}
