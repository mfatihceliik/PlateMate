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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plate_reports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_plate_reports_plate_user_type", columnNames = {
                        "plate_id", "user_id", "report_type_id"
                })
        },
        indexes = {
                @Index(name = "idx_plate_reports_active_last_reported_at", columnList = "active,last_reported_at"),
                @Index(name = "idx_plate_reports_plate_active_last_reported_at", columnList = "plate_id,active,last_reported_at"),
                @Index(name = "idx_plate_reports_type_active_last_reported_at", columnList = "report_type_id,active,last_reported_at")
        })
@Getter
@Setter
@NoArgsConstructor
public class PlateReport implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id", nullable = false)
    private Plate plate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id", nullable = false)
    private PlateReportType reportType;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "first_reported_at", nullable = false)
    private LocalDateTime firstReportedAt = LocalDateTime.now();

    @Column(name = "last_reported_at", nullable = false)
    private LocalDateTime lastReportedAt = LocalDateTime.now();

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
