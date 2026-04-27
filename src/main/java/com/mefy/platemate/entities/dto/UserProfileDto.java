package com.mefy.platemate.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String bio;
    private Double driverRating;
    private Integer reviewCount;
    private List<SocialMediaLinkDto> socialMediaLinks; // Aşağıdaki DTO'yu kullanıyor
    private Page<UserReviewDto> reviews; // Yorumlar da sayfalı olarak profille birlikte geliyor
}
