package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestReason;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
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
    private PlateRemovalRequestReason reason;
    private String description;
    private PlateRemovalRequestStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
}
