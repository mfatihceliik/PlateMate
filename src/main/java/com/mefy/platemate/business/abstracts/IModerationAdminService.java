package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateAdminDto;
import com.mefy.platemate.entities.dto.PlateReviewAdminDto;

public interface IModerationAdminService {
    DataResult<PagedData<PlateReviewAdminDto>> getPendingComments(PaginationRequest paginationRequest);

    Result approveComment(Long commentId, Long adminUserId);

    Result rejectComment(Long commentId, Long adminUserId, String reason);

    Result removeComment(Long commentId, Long adminUserId, String reason);

    DataResult<PagedData<PlateAdminDto>> getHiddenPlates(PaginationRequest paginationRequest);

    Result hidePlate(Long plateId, Long adminUserId, String reason);

    Result restorePlate(Long plateId, Long adminUserId);
}
