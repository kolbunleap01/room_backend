package kh.edu.roomrental.room;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :roomId")
    java.util.Optional<Room> findByIdForUpdate(@Param("roomId") UUID roomId);

    @Query("""
            select r from Room r
            where r.status = :status
              and (:query is null or lower(r.title) like lower(concat('%', :query, '%'))
                   or lower(r.description) like lower(concat('%', :query, '%'))
                   or lower(r.city) like lower(concat('%', :query, '%'))
                   or lower(coalesce(r.district, '')) like lower(concat('%', :query, '%')))
              and (:city is null or lower(r.city) = lower(:city))
              and (:minRent is null or r.monthlyRent >= :minRent)
              and (:maxRent is null or r.monthlyRent <= :maxRent)
            """)
    Page<Room> search(@Param("status") RoomStatus status,
                      @Param("query") String query,
                      @Param("city") String city,
                      @Param("minRent") BigDecimal minRent,
                      @Param("maxRent") BigDecimal maxRent,
                      Pageable pageable);

    Page<Room> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    long countByStatus(RoomStatus status);

    long countByOwnerId(UUID ownerId);
}
