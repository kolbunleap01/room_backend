package kh.edu.roomrental.config;

import kh.edu.roomrental.model.Role;
import kh.edu.roomrental.model.RoomListing;
import kh.edu.roomrental.model.UserAccount;
import kh.edu.roomrental.repository.RoomRepository;
import kh.edu.roomrental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapData {
    @Bean
    CommandLineRunner bootstrap(UserRepository users, RoomRepository rooms, PasswordEncoder encoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        return args -> {
            UserAccount admin = null;
            if (!adminEmail.isBlank() && !adminPassword.isBlank()) {
                admin = users.findByEmailIgnoreCase(adminEmail).orElseGet(() -> {
                    UserAccount account = new UserAccount();
                    account.setFullName("Room Rental Admin");
                    account.setEmail(adminEmail.trim().toLowerCase());
                    account.setPasswordHash(encoder.encode(adminPassword));
                    account.setRole(Role.ADMIN);
                    return users.save(account);
                });
            }
            if (rooms.count() == 0 && admin != null) {
                addRoom(rooms, admin, "Bright studio near university", "Furnished studio with Wi-Fi and private bathroom.", 180, "Tuol Kork, Phnom Penh", "Studio");
                addRoom(rooms, admin, "Quiet room with balcony", "Private room in a shared home with kitchen access.", 125, "BKK 1, Phnom Penh", "Private room");
                addRoom(rooms, admin, "One-bedroom apartment", "Apartment with separate kitchen and secure parking.", 260, "Sen Sok, Phnom Penh", "Apartment");
            }
        };
    }

    private void addRoom(RoomRepository repo, UserAccount owner, String title, String description, long price, String location, String type) {
        RoomListing room = new RoomListing();
        room.setOwner(owner);
        room.setTitle(title);
        room.setDescription(description);
        room.setPricePerMonth(price);
        room.setLocation(location);
        room.setRoomType(type);
        room.setAvailable(true);
        repo.save(room);
    }
}
