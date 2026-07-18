package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.UserProfilePageDto;
import com.mefy.platemate.entities.dto.request.UpdateProfileRequest;

public interface IUserProfileService {
    DataResult<UserProfileDto> getByUserId(Long userId, Long requesterUserId);

    DataResult<UserProfilePageDto> getPageByUserId(Long userId, Long requesterUserId);

    Result updateProfile(Long userId, UpdateProfileRequest request);
}
