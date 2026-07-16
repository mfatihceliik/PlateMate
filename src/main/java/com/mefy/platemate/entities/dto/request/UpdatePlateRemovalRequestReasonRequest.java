package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlateRemovalRequestReasonRequest implements IDto {
    @NotBlank(message = "{validation.plate.removal.reason.code.notblank}")
    private String code;

    @NotBlank(message = "{validation.plate.removal.reason.label.notblank}")
    private String label;

    @NotNull(message = "{validation.plate.removal.reason.requires.notnull}")
    private Boolean requiresDescription;

    @NotNull(message = "{validation.plate.removal.reason.sort.notnull}")
    @Min(value = 1, message = "{validation.plate.removal.reason.sort.min}")
    private Integer sortOrder;
}
