package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlateDetailDto implements IDto {
    // 1. Core Plate Info
    private Long id;
    private String plateCode;
    private String cityName;
    private Double ratingAverage;
    private Integer reviewCount;
    private Long totalRatingSum;

    // 2. Discovery Metrics (from DiscoveryPlateCardDto)
    private Long totalSearchCount;
    private Long totalReviewCount;
    private Long totalReportCount;
    private Long totalWeightedReportScore;
    private Double score;
    private LocalDateTime lastActivityAt;

    // 3. Detailed Data
    private List<PlateReviewDto> recentReviews;
    private List<PlateReportTypeDto> recentReportTypes;
}
