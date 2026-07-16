package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "plate_removal_requests",
        indexes = {
                @Index(name = "idx_plate_removal_requests_plate_status_id", columnList = "plate_id,status_id"),
                @Index(name = "idx_plate_removal_requests_status_id_created_at", columnList = "status_id,created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PlateRemovalRequest implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id", nullable = false)
    private Plate plate;

    @Column(name = "requester_user_id")
    private Long requesterUserId;

    @Column(name = "requester_email", length = 255)
    private String requesterEmail;

    @Column(name = "requester_username", length = 255)
    private String requesterUsername;

    @Column(name = "reason_code", nullable = false, length = 64)
    private String reasonCode;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "status_id", nullable = false)
    private Long statusId = PlateRemovalRequestStatus.OPEN.getId();

    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Transient
    public PlateRemovalRequestStatus getStatus() {
        return PlateRemovalRequestStatus.fromId(statusId);
    }

    public void setStatus(PlateRemovalRequestStatus status) {
        this.statusId = status == null ? null : status.getId();
    }

    @Transient
    public String getStatusCode() {
        PlateRemovalRequestStatus status = PlateRemovalRequestStatus.fromId(statusId);
        return status == null ? null : status.getCode();
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (statusId == null) statusId = PlateRemovalRequestStatus.OPEN.getId();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (statusId == null) statusId = PlateRemovalRequestStatus.OPEN.getId();
    }
}
