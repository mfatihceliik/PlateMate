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
    private PlateReportSeverity severity;
    private String colorHex;
    private Integer weight;
    private Integer sortOrder;
}
