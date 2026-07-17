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
public class UpdateDiscoveryTabOptionRequest implements IDto {
    @NotBlank(message = "{validation.discovery.tab.option.code.notblank}")
    private String code;

    @NotBlank(message = "{validation.discovery.tab.option.label.notblank}")
    private String label;

    @NotNull(message = "{validation.discovery.tab.option.sort.notnull}")
    @Min(value = 0, message = "{validation.discovery.tab.option.sort.min}")
    private Integer sortOrder;
}
