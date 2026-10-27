package kh.edu.roomrental.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final int generalLimit;
    private final int loginLimit;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper objectMapper,
                           @Value("${app.rate-limit.requests-per-minute:120}") int generalLimit,
                           @Value("${app.rate-limit.login-requests-per-minute:10}") int loginLimit) {
        this.objectMapper = objectMapper;
        this.generalLimit = generalLimit;
        this.loginLimit = loginLimit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        boolean login = "/api/v1/auth/login".equals(request.getRequestURI())
                || "/api/v1/auth/register".equals(request.getRequestURI());
        String key = (login ? "login:" : "general:") + ip;
        int limit = login ? loginLimit : generalLimit;
        Counter counter = counters.compute(key, (ignored, current) -> {
            long now = System.currentTimeMillis();
            if (current == null || now - current.windowStart > 60_000) {
                return new Counter(now, 1);
            }
            return new Counter(current.windowStart, current.count + 1);
        });

        if (counter.count > limit) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", "Too many requests. Try again later."
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private record Counter(long windowStart, int count) {
    }
}
