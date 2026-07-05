package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
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
public class UpdateSocialPlatformRequest implements IDto {
    @NotBlank(message = "{validation.social.platform.code.notblank}")
    private String code;

    @NotBlank(message = "{validation.social.platform.label.notblank}")
    private String label;

    private String iconUrl;

    private String baseUrl;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$", message = "{validation.social.platform.color.invalid}")
    private String backgroundColorHex;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$", message = "{validation.social.platform.color.invalid}")
    private String iconTintColorHex;

    @NotNull(message = "{validation.social.platform.sort.notnull}")
    @Min(value = 1, message = "{validation.social.platform.sort.min}")
    private Integer sortOrder;
}
