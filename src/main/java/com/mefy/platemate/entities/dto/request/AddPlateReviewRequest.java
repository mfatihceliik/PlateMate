package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
@NoArgsConstructor
public class AddPlateReviewRequest implements IDto {
    @NotNull(message = "{validation.review.rating.notnull}")
    @Min(value = 1, message = "{validation.review.rating.min}")
    @Max(value = 5, message = "{validation.review.rating.max}")
    private Integer rating;

    @Size(max = 250, message = "{validation.review.comment.max}")
    private String comment;

    private List<String> reportTypeCodes;

    private Boolean acceptedResponsibility;

    private String responsibilityPolicyVersion;

    public AddPlateReviewRequest(Integer rating, String comment, List<String> reportTypeCodes) {
        this(rating, comment, reportTypeCodes, null);
    }

    public AddPlateReviewRequest(
            Integer rating,
            String comment,
            List<String> reportTypeCodes,
            Boolean acceptedResponsibility
    ) {
        this(rating, comment, reportTypeCodes, acceptedResponsibility, null);
    }

    public AddPlateReviewRequest(
            Integer rating,
            String comment,
            List<String> reportTypeCodes,
            Boolean acceptedResponsibility,
            String responsibilityPolicyVersion
    ) {
        this.rating = rating;
        this.comment = comment;
        this.reportTypeCodes = reportTypeCodes;
        this.acceptedResponsibility = acceptedResponsibility;
        this.responsibilityPolicyVersion = responsibilityPolicyVersion;
    }
}
