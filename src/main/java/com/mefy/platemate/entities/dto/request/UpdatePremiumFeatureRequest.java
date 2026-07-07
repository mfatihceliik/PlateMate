package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePremiumFeatureRequest implements IDto {
    @NotBlank(message = "{validation.premium.icon.notblank}")
    private String iconKey;

    @NotNull(message = "{validation.premium.title.notblank}")
    private Map<String, String> titles;

    private Map<String, String> subtitles;

    @NotNull(message = "{validation.premium.sort.notnull}")
    @Min(value = 0, message = "{validation.premium.sort.min}")
    private Integer sortOrder;
}
