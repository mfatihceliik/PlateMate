package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateSettingsRequest;

public interface IUserSettingsService {
    DataResult<UserSettingsDto> getByUserId(Long userId);
    Result updateSettings(Long userId, UpdateSettingsRequest request);
}
