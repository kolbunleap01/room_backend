package kh.edu.roomrental.auth;

import kh.edu.roomrental.user.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private static final String SECRET = "VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdLZXlGb3JUb29tUmVudGFsQXBw";

    @Test
    void rejectsInvalidSecret() {
        assertThrows(RuntimeException.class, () -> new JwtService("bad", Duration.ofMinutes(15)));
    }

    @Test
    void issuesAndParsesUserIdentity() {
        JwtService service = new JwtService(SECRET, Duration.ofMinutes(15));
        User user = new User("user@example.com", "hash", "Room User", null);

        var claims = service.parse(service.issueAccessToken(user));

        assertEquals(user.getId(), service.userId(claims));
        assertEquals("user@example.com", claims.getSubject());
    }
}
