package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVehicleRequest implements IDto {
    @NotNull
    private Long id;

    @NotBlank(message = "{validation.vehicle.brand.notblank}")
    private String brand;

    @NotBlank(message = "{validation.vehicle.model.notblank}")
    private String model;

    private String color;

    @NotBlank(message = "{validation.vehicle.plate.notblank}")
    private String plateCode;

    @NotNull(message = "{validation.vehicle.city.notnull}")
    private Integer cityId;
}
