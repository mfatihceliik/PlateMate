package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserLocationDto;

import java.util.List;

public interface IUserLocationService {
    DataResult<List<Long>> updateLocation(Long userId, Double latitude, Double longitude);
    DataResult<UserLocationDto> getLocationByUserId(Long userId);
    DataResult<List<UserLocationDto>> getVisibleLocationsForUser(Long viewerId);
    
    Result blockUserFromLocation(Long userId, Long targetUserId);
    Result unblockUserFromLocation(Long userId, Long targetUserId);
    DataResult<List<Long>> getBlockedUserIds(Long userId);
}
