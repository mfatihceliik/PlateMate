package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import org.springframework.data.domain.Page;

public interface IPlateService {
    DataResult<PlateDto> searchByPlateCode(String plateCode);

    DataResult<Page<PlateReviewDto>> getReviewsByPlateCode(String plateCode, int page, int size);

    Result addReview(String plateCode, Long currentUserId, AddPlateReviewRequest request);

    Result updateReview(Long reviewId, Long currentUserId, UpdatePlateReviewRequest request);

    Result deleteReview(Long reviewId, Long currentUserId);
}
