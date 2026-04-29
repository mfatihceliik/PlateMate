package com.mefy.platemate.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDto implements IDto {
    @JsonProperty("id") // Android tarafında ID'yi net görmek için
    private Long id;
    private String plateCode;
    private String brand;
    private String model;
    private String color;
    private String cityName; // City entity'sinden name'i buraya çekeceğiz
}
