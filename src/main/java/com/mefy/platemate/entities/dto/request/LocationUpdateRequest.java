package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationUpdateRequest implements IDto {

    @NotNull(message = "{validation.location.latitude.notnull}")
    private Double latitude;

    @NotNull(message = "{validation.location.longitude.notnull}")
    private Double longitude;
}
