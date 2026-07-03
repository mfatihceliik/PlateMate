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
public class PlateDetailReviewDto implements IDto {
    private Long id;
    private Long userId;
    private String username;
    private String displayName;
    private String profilePhotoUrl;
    private Integer rating;
    private String comment;
    private List<String> reportTags;
    private LocalDateTime createdAt;
}
