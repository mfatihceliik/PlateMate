package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryPlateCardDto implements IDto {
    private String plateCode;
    private String cityName;
    private Double ratingAverage;
    private Integer reviewCount;
    private Long todaySearchCount;
    private Long todayReviewCount;
    private Long todayReportCount;
    private Long todayWeightedReportScore;
    private Double score;
    private LocalDateTime lastActivityAt;
    private java.util.List<PlateReportTypeDto> topReportTypes;
}
