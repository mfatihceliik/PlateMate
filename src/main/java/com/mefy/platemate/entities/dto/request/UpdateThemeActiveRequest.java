package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateThemeActiveRequest implements IDto {
    @NotNull(message = "{validation.accent.color.active.notnull}")
    private Boolean active;
}
