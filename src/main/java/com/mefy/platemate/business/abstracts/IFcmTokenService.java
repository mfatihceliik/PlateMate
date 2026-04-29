package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;

public interface IFcmTokenService {
    Result registerToken(Long userId, String token, String deviceId);
    Result unregisterToken(String token);
}
