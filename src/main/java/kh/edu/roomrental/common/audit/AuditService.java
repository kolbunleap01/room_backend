package kh.edu.roomrental.common.audit;

import kh.edu.roomrental.common.security.SecurityEventService;
import kh.edu.roomrental.common.security.SecurityEventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final SecurityEventService securityEventService;

    public AuditService(AuditLogRepository auditLogRepository, SecurityEventService securityEventService) {
        this.auditLogRepository = auditLogRepository;
        this.securityEventService = securityEventService;
    }

    @Transactional
    public void record(UUID actorUserId, String eventType, String ipAddress, String details) {
        String safeEventType = trim(eventType, 80);
        String safeIpAddress = trim(ipAddress, 64);
        String safeDetails = trim(details, 500);
        auditLogRepository.save(new AuditLog(actorUserId, safeEventType, safeIpAddress, safeDetails));
        securityEventService.record(actorUserId, safeEventType, safeIpAddress, statusFor(eventType), safeDetails);
    }

    private SecurityEventStatus statusFor(String eventType) {
        if (eventType != null && (eventType.endsWith("_FAILED") || eventType.endsWith("_BLOCKED")
                || eventType.endsWith("_DENIED") || eventType.equals("REFRESH_REUSE_OR_EXPIRED"))) {
            return SecurityEventStatus.FAILURE;
        }
        if (eventType != null && (eventType.endsWith("_SUCCEEDED") || eventType.endsWith("_SUCCESS"))) {
            return SecurityEventStatus.SUCCESS;
        }
        return SecurityEventStatus.INFO;
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
