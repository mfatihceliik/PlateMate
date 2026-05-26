package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewStatusCountsDto implements IDto {
    private Integer approved;
    private Integer pendingReview;
    private Integer rejected;
    private Integer removedByUser;
    private Integer removedByModerator;
    private Integer removedByLegalRequest;
}
