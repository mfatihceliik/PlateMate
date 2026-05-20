package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plate_report_types", indexes = {
        @Index(name = "idx_plate_report_types_active_sort", columnList = "active,sort_order")
})
@Getter
@Setter
@NoArgsConstructor
public class PlateReportType implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(name = "icon_key", nullable = false, length = 64)
    private String iconKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PlateReportSeverity severity;

    @Column(name = "color_hex", nullable = false, length = 16)
    private String colorHex;

    @Column(nullable = false)
    private Integer weight;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
