package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

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

    @Column(name = "severity_id", nullable = false)
    private Long severityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "severity_id", insertable = false, updatable = false)
    private PlateReportSeverityLookup severityRef;

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

    @Transient
    public PlateReportSeverity getSeverity() {
        PlateReportSeverity fromId = PlateReportSeverity.fromId(severityId);
        if (fromId != null) {
            return fromId;
        }
        return PlateReportSeverity.fromCode(resolveSeverityCodeFromRef());
    }

    public void setSeverity(PlateReportSeverity severity) {
        this.severityId = severity == null ? null : severity.getId();
    }

    @Transient
    public String getSeverityCode() {
        PlateReportSeverity severity = PlateReportSeverity.fromId(severityId);
        if (severity != null) {
            return severity.getCode();
        }
        return resolveSeverityCodeFromRef();
    }

    private String resolveSeverityCodeFromRef() {
        if (severityRef == null || !Hibernate.isInitialized(severityRef)) {
            return null;
        }
        return severityRef.getCode();
    }
}
