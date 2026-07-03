package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.PlateReview;

public interface IPlateFollowService {

    Result followPlate(Long userId, String plateCode);

    Result unfollowPlate(Long userId, String plateCode);

    void notifyReviewApproved(PlateReview review);
}
