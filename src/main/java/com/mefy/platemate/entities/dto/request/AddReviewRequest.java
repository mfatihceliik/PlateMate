package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddReviewRequest implements IDto {
    @NotNull(message = "{validation.review.target.notnull}")
    private Long targetProfileId;

    @NotNull(message = "{validation.review.rating.notnull}")
    @Min(value = 1, message = "{validation.review.rating.min}")
    @Max(value = 5, message = "{validation.review.rating.max}")
    private Integer rating;

    @NotBlank(message = "{validation.review.comment.notblank}")
    private String comment;
}
