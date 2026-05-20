package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto implements IDto {
    private Long id;
    private String username;
    private Double driverRating;
    private Integer reviewCount;
    private Long totalRatingSum;
    private List<SocialMediaLinkDto> socialMediaLinks;
    private PagedData<PlateReviewDto> plateReviews;
}
