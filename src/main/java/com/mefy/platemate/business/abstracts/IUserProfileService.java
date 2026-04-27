package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserProfileDto;

public interface IUserProfileService {
    DataResult<UserProfileDto> getByUserId(Long userId, int page, int size);
    Result update(UserProfile userProfile);
}
