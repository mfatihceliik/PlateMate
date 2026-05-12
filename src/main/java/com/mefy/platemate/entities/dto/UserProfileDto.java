package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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
    private Page<PlateReviewDto> plateReviews;
}
