package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlateReviewAdminDto implements IDto {
    private Long id;
    private String plateCode;
    private Long userId;
    private String username;
    private Integer rating;
    private String comment;
    private Long statusId;
    private String statusCode;
    private String moderationReason;
    private Integer reportCount;
    private Boolean userAcceptedResponsibility;
    private String responsibilityPolicyVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
