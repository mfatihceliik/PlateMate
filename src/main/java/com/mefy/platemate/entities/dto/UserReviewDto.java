package com.mefy.platemate.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewDto {
    private Long id;
    private Integer rating;
    private String comment;
    private String reviewerUsername; // Yorumu yapanın nick'i
    private LocalDateTime createdAt;
}
