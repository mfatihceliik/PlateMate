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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "plate_review_moderation_events",
        indexes = {
                @Index(name = "idx_prme_review_created_at", columnList = "plate_review_id,created_at"),
                @Index(name = "idx_prme_to_status_id_created_at", columnList = "to_status_id,created_at"),
                @Index(name = "idx_prme_actor_created_at", columnList = "actor_user_id,created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PlateReviewModerationEvent implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_review_id", nullable = false)
    private PlateReview plateReview;

    @Column(name = "from_status_id")
    private Long fromStatusId;

    @Column(name = "to_status_id", nullable = false)
    private Long toStatusId;

    @Column(name = "action_type_id", nullable = false)
    private Long actionTypeId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    public PlateReviewStatus getFromStatus() {
        return PlateReviewStatus.fromId(fromStatusId);
    }

    public void setFromStatus(PlateReviewStatus fromStatus) {
        this.fromStatusId = fromStatus == null ? null : fromStatus.getId();
    }

    @Transient
    public PlateReviewStatus getToStatus() {
        return PlateReviewStatus.fromId(toStatusId);
    }

    public void setToStatus(PlateReviewStatus toStatus) {
        this.toStatusId = toStatus == null ? null : toStatus.getId();
    }

    @Transient
    public PlateReviewModerationActionType getActionType() {
        return PlateReviewModerationActionType.fromId(actionTypeId);
    }

    public void setActionType(PlateReviewModerationActionType actionType) {
        this.actionTypeId = actionType == null ? null : actionType.getId();
    }

    @Transient
    public String getActionTypeCode() {
        PlateReviewModerationActionType actionType = PlateReviewModerationActionType.fromId(actionTypeId);
        return actionType == null ? null : actionType.getCode();
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
