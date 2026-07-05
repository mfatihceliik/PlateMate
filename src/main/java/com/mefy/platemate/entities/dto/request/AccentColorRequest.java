package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Shared body for adding and updating an accent color. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccentColorRequest implements IDto {
    @NotBlank(message = "{validation.accent.color.hex.notblank}")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "{validation.accent.color.hex.invalid}")
    private String hex;

    @NotNull(message = "{validation.accent.color.sort.notnull}")
    @Min(value = 0, message = "{validation.accent.color.sort.min}")
    private Integer sortOrder;
}
