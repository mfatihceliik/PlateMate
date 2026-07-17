package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDiscoveryTabOptionActiveRequest implements IDto {
    @NotNull(message = "{validation.discovery.tab.option.active.notnull}")
    private Boolean active;
}
