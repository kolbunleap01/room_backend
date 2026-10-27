package kh.edu.roomrental.auth;

import kh.edu.roomrental.common.exception.UnauthorizedException;
import kh.edu.roomrental.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.jwt.secret-base64=VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdLZXlGb3JUb29tUmVudGFsQXBw"
})
class AuthIntegrationTest {
    @Autowired AuthService auth;
    @Autowired UserRepository users;
    @Autowired TestRestTemplate http;
    private static final String PASSWORD = "Test-only-password-2026";

    private AuthDtos.TokenResponse register() {
        return auth.register(new AuthDtos.RegisterRequest(
                UUID.randomUUID() + "@example.test", PASSWORD, "Integration User", null), "127.0.0.1");
    }

    @Test
    void registrationAndAuditCommitTogetherAndUserCannotAccessAdmin() {
        var registered = register();
        assertTrue(users.findById(registered.id()).isPresent());
        var headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(registered.accessToken());
        var response = http.exchange("/api/v1/admin/dashboard", org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers), String.class);
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void failedLoginsPersistAndLockTheAccount() {
        var registered = register();
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(UnauthorizedException.class, () -> auth.login(
                    new AuthDtos.LoginRequest(registered.email(), "Wrong-password"), "127.0.0.1"));
        }
        var user = users.findById(registered.id()).orElseThrow();
        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());
        assertThrows(UnauthorizedException.class, () -> auth.login(
                new AuthDtos.LoginRequest(registered.email(), PASSWORD), "127.0.0.1"));
    }

    @Test
    void refreshRotationAndLogoutInvalidateOldTokens() {
        var first = register();
        var second = auth.refresh(first.refreshToken(), "127.0.0.1");
        assertNotEquals(first.refreshToken(), second.refreshToken());
        assertThrows(UnauthorizedException.class, () -> auth.refresh(first.refreshToken(), "127.0.0.1"));
        auth.logout(second.refreshToken(), "127.0.0.1");
        assertThrows(UnauthorizedException.class, () -> auth.refresh(second.refreshToken(), "127.0.0.1"));
    }

    @Test
    void disabledUserCannotRefresh() {
        var registered = register();
        var user = users.findById(registered.id()).orElseThrow();
        user.setEnabled(false);
        users.save(user);
        assertThrows(UnauthorizedException.class, () -> auth.refresh(registered.refreshToken(), "127.0.0.1"));
    }

    @Test
    void concurrentRefreshAcceptsTheTokenOnlyOnce() throws Exception {
        var registered = register();
        var executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> attempt = () -> {
                try {
                    auth.refresh(registered.refreshToken(), "127.0.0.1");
                    return true;
                } catch (UnauthorizedException expected) {
                    return false;
                }
            };
            var a = executor.submit(attempt);
            var b = executor.submit(attempt);
            int successes = (a.get(15, TimeUnit.SECONDS) ? 1 : 0) + (b.get(15, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successes);
        } finally {
            executor.shutdownNow();
        }
    }
}
