package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.CommentReportReason;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
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
    private CommentReportReason reason;
    private String description;
    private CommentReportStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
}
