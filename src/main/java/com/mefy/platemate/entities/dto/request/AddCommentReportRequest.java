package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentReportRequest implements IDto {
    private Long reasonId;

    private String reasonCode;

    @Size(max = 1000, message = "{validation.comment.report.description.max}")
    private String description;
}
