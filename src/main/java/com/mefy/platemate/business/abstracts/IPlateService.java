package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.SyncPlateReportsRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;

public interface IPlateService {


    DataResult<PagedData<PlateReviewDto>> getReviewsByPlateCode(String plateCode, PaginationRequest paginationRequest);

    Result addReview(String plateCode, Long currentUserId, AddPlateReviewRequest request);

    Result updateReview(Long reviewId, Long currentUserId, UpdatePlateReviewRequest request);

    Result deleteReview(Long reviewId, Long currentUserId);

    Result syncReports(String plateCode, Long currentUserId, SyncPlateReportsRequest request);
}
