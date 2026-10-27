package kh.edu.roomrental.repository;

import kh.edu.roomrental.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserEmailIgnoreCase(String email);
    Optional<Favorite> findByUserEmailIgnoreCaseAndRoomId(String email, Long roomId);
}
