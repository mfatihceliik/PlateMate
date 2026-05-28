package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlateRemovalRequestDto implements IDto {
    private Long id;
    private Long plateId;
    private String plateCode;
    private Long requesterUserId;
    private String requesterEmail;
    private Long reasonId;
    private String reasonCode;
    private String description;
    private Long statusId;
    private String statusCode;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
}
