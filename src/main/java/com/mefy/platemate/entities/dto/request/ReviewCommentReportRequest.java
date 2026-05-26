package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentReportRequest implements IDto {
    @NotNull(message = "{validation.comment.report.status.notnull}")
    private CommentReportStatus status;

    @Size(max = 1000, message = "{validation.comment.report.admin.note.max}")
    private String adminNote;
}
