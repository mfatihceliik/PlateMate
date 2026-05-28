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
@Table(name = "plates",
        indexes = {
                @Index(name = "idx_plates_status_id", columnList = "status_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class Plate implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String plateCode;

    @Column(name = "status_id", nullable = false)
    private Long statusId = PlateStatus.ACTIVE.getId();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private PlateStatusLookup statusRef;

    @Column(length = 500)
    private String hiddenReason;

    @Column
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(nullable = false)
    private Double ratingAverage = 0.0;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Long totalRatingSum = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Transient
    public PlateStatus getStatus() {
        PlateStatus fromId = PlateStatus.fromId(statusId);
        if (fromId != null) {
            return fromId;
        }
        return PlateStatus.fromCode(resolveStatusCodeFromRef());
    }

    public void setStatus(PlateStatus status) {
        this.statusId = status == null ? null : status.getId();
    }

    @Transient
    public String getStatusCode() {
        PlateStatus status = PlateStatus.fromId(statusId);
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
        if (statusId == null) statusId = PlateStatus.ACTIVE.getId();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (statusId == null) statusId = PlateStatus.ACTIVE.getId();
    }
}
