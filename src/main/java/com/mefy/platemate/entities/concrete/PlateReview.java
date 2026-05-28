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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;

@Entity
@Table(name = "plate_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "plate_id"})
        },
        indexes = {
                @Index(name = "idx_plate_reviews_status_id", columnList = "status_id"),
                @Index(name = "idx_plate_reviews_status_id_created_at", columnList = "status_id,created_at"),
                @Index(name = "idx_plate_reviews_created_at", columnList = "created_at"),
                @Index(name = "idx_plate_reviews_user_id_status_id", columnList = "user_id,status_id"),
                @Index(name = "idx_plate_reviews_user_id_created_at", columnList = "user_id,created_at"),
                @Index(name = "idx_plate_reviews_plate_id_status_id", columnList = "plate_id,status_id"),
                @Index(name = "idx_plate_reviews_plate_id_created_at", columnList = "plate_id,created_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class PlateReview implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id", nullable = false)
    private Plate plate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String comment;

    @Column(name = "status_id", nullable = false)
    private Long statusId = PlateReviewStatus.PENDING_REVIEW.getId();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private PlateReviewStatusLookup statusRef;

    @Column(length = 255)
    private String moderationReason;

    @Column(nullable = false)
    private Integer reportCount = 0;

    @Column(name = "user_accepted_responsibility", nullable = false)
    private Boolean userAcceptedResponsibility = false;

    @Column(name = "user_accepted_responsibility_at")
    private LocalDateTime userAcceptedResponsibilityAt;

    @Column(name = "responsibility_policy_version", length = 32)
    private String responsibilityPolicyVersion;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "user_agent_hash", length = 64)
    private String userAgentHash;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Transient
    public PlateReviewStatus getStatus() {
        PlateReviewStatus fromId = PlateReviewStatus.fromId(statusId);
        if (fromId != null) {
            return fromId;
        }
        return PlateReviewStatus.fromCode(resolveStatusCodeFromRef());
    }

    public void setStatus(PlateReviewStatus status) {
        this.statusId = status == null ? null : status.getId();
    }

    @Transient
    public String getStatusCode() {
        PlateReviewStatus status = PlateReviewStatus.fromId(statusId);
        if (status != null) {
            return status.getCode();
        }
        return resolveStatusCodeFromRef();
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
        if (statusId == null) statusId = PlateReviewStatus.PENDING_REVIEW.getId();
        if (reportCount == null) reportCount = 0;
        if (userAcceptedResponsibility == null) userAcceptedResponsibility = false;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (statusId == null) statusId = PlateReviewStatus.PENDING_REVIEW.getId();
        if (reportCount == null) reportCount = 0;
        if (userAcceptedResponsibility == null) userAcceptedResponsibility = false;
    }
}
