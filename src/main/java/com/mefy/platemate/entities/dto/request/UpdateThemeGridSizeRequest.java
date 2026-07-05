package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateThemeGridSizeRequest implements IDto {
    @NotNull(message = "{validation.theme.gridsize.notnull}")
    @Min(value = 1, message = "{validation.theme.gridsize.range}")
    @Max(value = 8, message = "{validation.theme.gridsize.range}")
    private Integer gridSize;
}
