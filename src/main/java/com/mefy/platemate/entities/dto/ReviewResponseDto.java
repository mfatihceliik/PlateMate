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
public class ReviewResponseDto implements IDto {
    private Long reviewId;
    private String plateCode;
    private Integer rating;
    private String comment;
    private String status;
    private Long userId;
    private String username;
    private String displayName;
    private String profilePhotoUrl;
    private List<String> reportTypeCodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
