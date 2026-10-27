package kh.edu.roomrental.room;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "room_type", nullable = false, length = 20)
    private String roomType;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(precision = 12, scale = 2)
    private BigDecimal deposit;

    @Column(nullable = false)
    private int bedrooms;

    @Column(nullable = false)
    private int bathrooms;

    @Column(name = "area_sqm", precision = 10, scale = 2)
    private BigDecimal areaSqm;

    @Column(nullable = false)
    private boolean furnished;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Room() {
    }

    public Room(UUID ownerId, RoomDtos.CreateRoomRequest request) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.status = RoomStatus.ACTIVE;
        this.createdAt = Instant.now();
        apply(request);
    }

    public void update(RoomDtos.UpdateRoomRequest request) {
        apply(request);
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = RoomStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    private void apply(RoomDtos.RoomFields request) {
        this.title = request.title().trim();
        this.roomType = RoomType.normalize(request.roomType());
        this.description = request.description().trim();
        this.address = request.address().trim();
        this.city = request.city().trim();
        this.district = request.district() == null || request.district().isBlank() ? null : request.district().trim();
        this.monthlyRent = request.monthlyRent();
        this.deposit = request.deposit();
        this.bedrooms = request.bedrooms();
        this.bathrooms = request.bathrooms();
        this.areaSqm = request.areaSqm();
        this.furnished = request.furnished();
        this.availableFrom = request.availableFrom();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public int getBedrooms() {
        return bedrooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public BigDecimal getAreaSqm() {
        return areaSqm;
    }

    public boolean isFurnished() {
        return furnished;
    }

    public LocalDate getAvailableFrom() {
        return availableFrom;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
