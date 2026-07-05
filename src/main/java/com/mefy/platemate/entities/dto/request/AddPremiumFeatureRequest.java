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
public class AddPremiumFeatureRequest implements IDto {
    @NotBlank(message = "{validation.premium.icon.notblank}")
    private String iconKey;

    @NotBlank(message = "{validation.premium.title.notblank}")
    private String titleTr;

    @NotBlank(message = "{validation.premium.title.notblank}")
    private String titleEn;

    private String subtitleTr;

    private String subtitleEn;

    @NotNull(message = "{validation.premium.sort.notnull}")
    @Min(value = 0, message = "{validation.premium.sort.min}")
    private Integer sortOrder;
}
