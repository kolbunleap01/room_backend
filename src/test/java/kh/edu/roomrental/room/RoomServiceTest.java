package kh.edu.roomrental.room;

import kh.edu.roomrental.common.audit.AuditService;
import kh.edu.roomrental.common.exception.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RoomServiceTest {
    @Test
    void rejectsNonOwnerRoomMutation() {
        RoomRepository roomRepository = mock(RoomRepository.class);
        var userRepository = mock(kh.edu.roomrental.user.UserRepository.class);
        AuditService auditService = mock(AuditService.class);
        RoomService service = new RoomService(roomRepository, userRepository, auditService);
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Room room = new Room(ownerId, new RoomDtos.CreateRoomRequest("Room", "PRIVATE_ROOM", "Description", "Address", "City", null,
                BigDecimal.TEN, BigDecimal.ZERO, 1, 1, BigDecimal.ONE, false, LocalDate.now()));

        assertThrows(ForbiddenException.class, () -> service.ensureOwner(otherId, room));
    }

    @Test
    void normalizesDisplayRoomTypeForPersistenceAndResponses() {
        UUID ownerId = UUID.randomUUID();
        Room room = new Room(ownerId, new RoomDtos.CreateRoomRequest("Room", "Private Room", "Description",
                "Address", "City", null, BigDecimal.TEN, BigDecimal.ZERO, 1, 1, BigDecimal.ONE, false,
                LocalDate.now()));

        assertEquals("PRIVATE_ROOM", room.getRoomType());
    }
}
