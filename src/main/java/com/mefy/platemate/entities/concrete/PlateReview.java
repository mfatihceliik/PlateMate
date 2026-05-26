package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plate_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "plate_id"})
        },
        indexes = {
                @Index(name = "idx_plate_reviews_status", columnList = "status"),
                @Index(name = "idx_plate_reviews_status_created_at", columnList = "status,created_at"),
                @Index(name = "idx_plate_reviews_created_at", columnList = "created_at"),
                @Index(name = "idx_plate_reviews_user_id_status", columnList = "user_id,status"),
                @Index(name = "idx_plate_reviews_user_id_created_at", columnList = "user_id,created_at"),
                @Index(name = "idx_plate_reviews_plate_id_status", columnList = "plate_id,status"),
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PlateReviewStatus status = PlateReviewStatus.PENDING_REVIEW;

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

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = PlateReviewStatus.PENDING_REVIEW;
        if (reportCount == null) reportCount = 0;
        if (userAcceptedResponsibility == null) userAcceptedResponsibility = false;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == null) status = PlateReviewStatus.PENDING_REVIEW;
        if (reportCount == null) reportCount = 0;
        if (userAcceptedResponsibility == null) userAcceptedResponsibility = false;
    }
}
