package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.UserReview;
import com.mefy.platemate.entities.dto.UserReviewDto;

import org.springframework.data.domain.Page;

public interface IUserReviewService {
    Result add(UserReview userReview);
    DataResult<Page<UserReviewDto>> getByTargetProfileId(Long targetProfileId, int page, int size);
    Result delete(Long reviewId, Long currentUserId); // Sadece yorumu yazan silebilir
}
