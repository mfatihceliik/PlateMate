package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlateReportTypeDto implements IDto {
    private String code;
    private String label;
    private String description;
    private String iconKey;
    private Long severityId;
    private String severityCode;
    private String colorHex;
    private Integer weight;
    private Integer sortOrder;

    public PlateReportTypeDto(
            String code,
            String label,
            String description,
            String iconKey,
            PlateReportSeverity severity,
            String colorHex,
            Integer weight,
            Integer sortOrder
    ) {
        this.code = code;
        this.label = label;
        this.description = description;
        this.iconKey = iconKey;
        this.severityId = severity == null ? null : severity.getId();
        this.severityCode = severity == null ? null : severity.getCode();
        this.colorHex = colorHex;
        this.weight = weight;
        this.sortOrder = sortOrder;
    }
}
