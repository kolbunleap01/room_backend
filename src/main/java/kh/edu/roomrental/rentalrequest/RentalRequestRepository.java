package kh.edu.roomrental.rentalrequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;

import java.util.UUID;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RentalRequest r where r.id = :requestId")
    java.util.Optional<RentalRequest> findByIdForUpdate(@Param("requestId") UUID requestId);

    Page<RentalRequest> findByApplicantIdOrderByCreatedAtDesc(UUID applicantId, Pageable pageable);

    Page<RentalRequest> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    Page<RentalRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Transactional
    void deleteByRoomId(UUID roomId);

    boolean existsByRoomIdAndApplicantIdAndStatus(UUID roomId, UUID applicantId, RentalRequestStatus status);
}
