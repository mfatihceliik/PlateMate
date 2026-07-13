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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "comment_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_comment_reports_comment_reporter", columnNames = {"comment_id", "reporter_user_id"})
        },
        indexes = {
                @Index(name = "idx_comment_reports_comment_status_id", columnList = "comment_id,status_id"),
                @Index(name = "idx_comment_reports_status_id_created_at", columnList = "status_id,created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CommentReport implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private PlateReview comment;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "reason_id", nullable = false)
    private Long reasonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id", insertable = false, updatable = false)
    private CommentReportReason reason;

    @Column(length = 1000)
    private String description;

    @Column(name = "status_id", nullable = false)
    private Long statusId = CommentReportStatus.OPEN.getId();

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
    public String getReasonCode() {
        return reason == null ? null : reason.getCode();
    }

    @Transient
    public CommentReportStatus getStatus() {
        return CommentReportStatus.fromId(statusId);
    }

    public void setStatus(CommentReportStatus status) {
        this.statusId = status == null ? null : status.getId();
    }

    @Transient
    public String getStatusCode() {
        CommentReportStatus status = CommentReportStatus.fromId(statusId);
        return status == null ? null : status.getCode();
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (statusId == null) statusId = CommentReportStatus.OPEN.getId();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (statusId == null) statusId = CommentReportStatus.OPEN.getId();
    }
}
