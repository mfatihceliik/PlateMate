package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.entities.dto.UserProfileDto;

public interface IUserProfileService {
    DataResult<UserProfileDto> getByUserId(Long userId, PaginationRequest paginationRequest);
}
