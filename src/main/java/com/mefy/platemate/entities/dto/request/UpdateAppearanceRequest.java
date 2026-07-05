package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Per-user appearance write-through: the client persists locally and mirrors it here. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAppearanceRequest implements IDto {
    @NotBlank(message = "{validation.appearance.mode.invalid}")
    @Pattern(regexp = "^(SYSTEM|LIGHT|DARK)$", message = "{validation.appearance.mode.invalid}")
    private String themeMode;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "{validation.appearance.accent.invalid}")
    private String accentHex; // nullable
}
