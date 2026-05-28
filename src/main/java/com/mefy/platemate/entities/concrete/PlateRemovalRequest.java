package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

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

    @Column(name = "reason_id", nullable = false)
    private Long reasonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id", insertable = false, updatable = false)
    private PlateRemovalRequestReasonLookup reasonRef;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "status_id", nullable = false)
    private Long statusId = PlateRemovalRequestStatus.OPEN.getId();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private PlateRemovalRequestStatusLookup statusRef;

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
    public PlateRemovalRequestReason getReason() {
        PlateRemovalRequestReason fromId = PlateRemovalRequestReason.fromId(reasonId);
        if (fromId != null) {
            return fromId;
        }
        return PlateRemovalRequestReason.fromCode(resolveReasonCodeFromRef());
    }

    public void setReason(PlateRemovalRequestReason reason) {
        this.reasonId = reason == null ? null : reason.getId();
    }

    @Transient
    public String getReasonCode() {
        PlateRemovalRequestReason reason = PlateRemovalRequestReason.fromId(reasonId);
        if (reason != null) {
            return reason.getCode();
        }
        return resolveReasonCodeFromRef();
    }

    @Transient
    public PlateRemovalRequestStatus getStatus() {
        PlateRemovalRequestStatus fromId = PlateRemovalRequestStatus.fromId(statusId);
        if (fromId != null) {
            return fromId;
        }
        return PlateRemovalRequestStatus.fromCode(resolveStatusCodeFromRef());
    }

    public void setStatus(PlateRemovalRequestStatus status) {
        this.statusId = status == null ? null : status.getId();
    }

    @Transient
    public String getStatusCode() {
        PlateRemovalRequestStatus status = PlateRemovalRequestStatus.fromId(statusId);
        if (status != null) {
            return status.getCode();
        }
        return resolveStatusCodeFromRef();
    }

    private String resolveReasonCodeFromRef() {
        if (reasonRef == null || !Hibernate.isInitialized(reasonRef)) {
            return null;
        }
        return reasonRef.getCode();
    }

    private String resolveStatusCodeFromRef() {
        if (statusRef == null || !Hibernate.isInitialized(statusRef)) {
            return null;
        }
        return statusRef.getCode();
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
