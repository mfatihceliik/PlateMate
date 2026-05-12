package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.UserSubscriptionDto;

import java.util.List;

public interface ISubscriptionService {
    DataResult<UserDto> activate(Long currentUserId, Integer days);

    DataResult<UserDto> getCurrentSubscription(Long currentUserId);

    DataResult<List<UserSubscriptionDto>> getMySubscriptionHistory(Long currentUserId);
}
