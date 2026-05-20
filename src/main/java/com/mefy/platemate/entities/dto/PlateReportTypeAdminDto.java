package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlateReportTypeAdminDto implements IDto {
    private Long id;
    private String code;
    private String label;
    private String description;
    private String iconKey;
    private PlateReportSeverity severity;
    private String colorHex;
    private Integer weight;
    private Integer sortOrder;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
