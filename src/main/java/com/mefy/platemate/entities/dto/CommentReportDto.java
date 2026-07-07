package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReportDto implements IDto {
    private Long id;
    private Long commentId;
    private Long reporterUserId;
    private String plateCode;
    private Long reasonId;
    private String reasonCode;
    private String reasonLabel;
    private String description;
    private Long statusId;
    private String statusCode;
    private String statusLabel;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
}
