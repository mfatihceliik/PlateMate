package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityPlateActivityDto implements IDto {
    private String plateCode;
    private Long todayReviewCount;
    private Long todayReportCount;
    private LocalDateTime lastActivityAt;
    private Double ratingAverage;
    private Integer reviewCount;
}
