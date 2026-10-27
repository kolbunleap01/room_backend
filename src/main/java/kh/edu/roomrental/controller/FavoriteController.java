package kh.edu.roomrental.controller;

import kh.edu.roomrental.model.Favorite;
import kh.edu.roomrental.model.RoomListing;
import kh.edu.roomrental.model.UserAccount;
import kh.edu.roomrental.repository.FavoriteRepository;
import kh.edu.roomrental.repository.RoomRepository;
import kh.edu.roomrental.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteRepository favorites;
    private final RoomRepository rooms;
    private final UserRepository users;

    public FavoriteController(FavoriteRepository favorites, RoomRepository rooms, UserRepository users) {
        this.favorites = favorites;
        this.rooms = rooms;
        this.users = users;
    }

    @GetMapping
    public List<Long> list(Authentication auth) {
        return favorites.findByUserEmailIgnoreCase(auth.getName()).stream().map(f -> f.getRoom().getId()).toList();
    }

    @PostMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(@PathVariable Long roomId, Authentication auth) {
        if (favorites.findByUserEmailIgnoreCaseAndRoomId(auth.getName(), roomId).isPresent()) return;
        UserAccount user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
        RoomListing room = rooms.findById(roomId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRoom(room);
        favorites.save(favorite);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long roomId, Authentication auth) {
        favorites.findByUserEmailIgnoreCaseAndRoomId(auth.getName(), roomId).ifPresent(favorites::delete);
    }
}
