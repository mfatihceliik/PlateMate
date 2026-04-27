package com.mefy.platemate.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddVehicleRequest {
    @NotBlank(message = "{validation.vehicle.plate.notblank}")
    private String plateCode;

    private String brand;
    private String model;
    private String color;

    @NotNull(message = "{validation.vehicle.city.notnull}")
    private Integer cityId;
}
