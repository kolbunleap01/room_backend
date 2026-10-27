package kh.edu.roomrental.repository;

import kh.edu.roomrental.model.RoomListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<RoomListing, Long> {
    List<RoomListing> findByAvailableTrueOrderByIdDesc();
    List<RoomListing> findAllByOrderByIdDesc();
    List<RoomListing> findByOwnerEmailIgnoreCaseOrderByIdDesc(String email);
}
