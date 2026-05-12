package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlateDto implements IDto {
    private Long id;
    private String plateCode;
    private String cityName;
    private Double ratingAverage = 0.0;
    private Integer reviewCount = 0;
    private Long totalRatingSum = 0L;
}
