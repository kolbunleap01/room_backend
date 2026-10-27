package kh.edu.roomrental.common.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SecurityEventService {
    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    @Transactional
    public void record(UUID actorUserId, String eventType, String ipAddress, SecurityEventStatus status,
                       String details) {
        securityEventRepository.save(new SecurityEvent(actorUserId, trim(eventType, 80), trim(ipAddress, 64), status,
                trim(details, 500)));
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
