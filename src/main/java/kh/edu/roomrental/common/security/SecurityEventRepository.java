package kh.edu.roomrental.common.security;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {
    Page<SecurityEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
