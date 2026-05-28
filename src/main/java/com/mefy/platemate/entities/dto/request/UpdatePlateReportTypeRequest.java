package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlateReportTypeRequest implements IDto {
    @NotBlank(message = "{validation.report.type.code.notblank}")
    private String code;

    @NotBlank(message = "{validation.report.type.label.notblank}")
    private String label;

    @NotBlank(message = "{validation.report.type.description.notblank}")
    private String description;

    @NotBlank(message = "{validation.report.type.icon.notblank}")
    private String iconKey;

    private Long severityId;

    private String severityCode;

    @NotBlank(message = "{validation.report.type.color.notblank}")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$", message = "{validation.report.type.color.invalid}")
    private String colorHex;

    @NotNull(message = "{validation.report.type.weight.notnull}")
    @Min(value = 1, message = "{validation.report.type.weight.min}")
    private Integer weight;

    @NotNull(message = "{validation.report.type.sort.notnull}")
    @Min(value = 1, message = "{validation.report.type.sort.min}")
    private Integer sortOrder;

    public UpdatePlateReportTypeRequest(
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
