package kh.edu.roomrental.repository;

import kh.edu.roomrental.model.RentalRequest;
import kh.edu.roomrental.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    List<RentalRequest> findByRequesterEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    List<RentalRequest> findByRoomOwnerEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    long countByStatus(RequestStatus status);
}
