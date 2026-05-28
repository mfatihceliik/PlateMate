package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentReportRequest implements IDto {
    private Long statusId;

    private String statusCode;

    @Size(max = 1000, message = "{validation.comment.report.admin.note.max}")
    private String adminNote;

    public ReviewCommentReportRequest(CommentReportStatus status, String adminNote) {
        this.statusId = status == null ? null : status.getId();
        this.statusCode = status == null ? null : status.getCode();
        this.adminNote = adminNote;
    }
}
