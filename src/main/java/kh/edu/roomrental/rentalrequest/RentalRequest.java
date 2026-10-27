package kh.edu.roomrental.rentalrequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rental_requests")
public class RentalRequest {
    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "applicant_id", nullable = false)
    private UUID applicantId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentalRequestStatus status;

    @Column(name = "decision_note", length = 2000)
    private String decisionNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RentalRequest() {
    }

    public RentalRequest(UUID roomId, UUID applicantId, UUID ownerId, RentalRequestDtos.CreateRequest request) {
        this.id = UUID.randomUUID();
        this.roomId = roomId;
        this.applicantId = applicantId;
        this.ownerId = ownerId;
        this.moveInDate = request.moveInDate();
        this.message = request.message() == null ? null : request.message().trim();
        this.status = RentalRequestStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void approve(String note) {
        this.status = RentalRequestStatus.APPROVED;
        this.decisionNote = note;
        this.updatedAt = Instant.now();
    }

    public void reject(String note) {
        this.status = RentalRequestStatus.REJECTED;
        this.decisionNote = note;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = RentalRequestStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getRoomId() { return roomId; }
    public UUID getApplicantId() { return applicantId; }
    public UUID getOwnerId() { return ownerId; }
    public LocalDate getMoveInDate() { return moveInDate; }
    public String getMessage() { return message; }
    public RentalRequestStatus getStatus() { return status; }
    public String getDecisionNote() { return decisionNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
