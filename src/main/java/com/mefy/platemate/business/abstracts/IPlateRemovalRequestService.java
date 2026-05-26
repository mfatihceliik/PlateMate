package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateRemovalRequestDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestRequest;
import com.mefy.platemate.entities.dto.request.ReviewPlateRemovalRequestRequest;

public interface IPlateRemovalRequestService {
    DataResult<PlateRemovalRequestDto> addRequest(
            Long plateId,
            Long requesterUserId,
            AddPlateRemovalRequestRequest request
    );

    DataResult<PagedData<PlateRemovalRequestDto>> getRequests(PaginationRequest paginationRequest);

    Result reviewRequest(Long requestId, Long reviewerUserId, ReviewPlateRemovalRequestRequest request);
}
