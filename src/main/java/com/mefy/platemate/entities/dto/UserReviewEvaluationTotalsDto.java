package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewEvaluationTotalsDto implements IDto {
    private Integer totalApproved;
    private Integer totalPendingReview;
    private Integer totalRejected;
    private Integer totalRemovedByUser;
    private Integer totalRemovedByModerator;
    private Integer totalRemovedByLegalRequest;
}
