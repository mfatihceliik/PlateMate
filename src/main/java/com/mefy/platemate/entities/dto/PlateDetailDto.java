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

    // 2. Rating Distribution (5-star to 1-star)
    private List<RatingDistributionDto> ratingDistribution;

    // 3. Aggregated Tag Counts
    private List<PlateTagSummaryDto> tagSummary;

    // 4. Discovery Metrics
    private Long totalSearchCount;
    private Long totalReviewCount;
    private Long totalReportCount;
    private Long totalWeightedReportScore;
    private Double score;
    private LocalDateTime lastActivityAt;

    // 5. Recent Reviews with User Info and Tags
    private List<PlateDetailReviewDto> recentReviews;

    // 6. Whether the current user follows this plate
    private Boolean following;
}
