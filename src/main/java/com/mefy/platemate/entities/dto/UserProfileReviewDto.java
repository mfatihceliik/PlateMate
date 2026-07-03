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
public class UserProfileReviewDto implements IDto {
    private Long id;
    private String plateCode;
    private String cityName;
    private Integer rating;
    private String comment;
    private Long reviewStatusId;
    private String reviewStatusCode;
    private List<String> reportTags;
    private LocalDateTime createdAt;
}
