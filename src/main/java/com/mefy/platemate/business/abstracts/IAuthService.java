package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.ChangePasswordRequest;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RefreshTokenRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;

public interface IAuthService {
    DataResult<UserDto> register(RegisterRequest request);
    DataResult<UserDto> login(LoginRequest request);
    DataResult<UserDto> refresh(RefreshTokenRequest request);
    Result logout(RefreshTokenRequest request);
    Result changePassword(Long userId, ChangePasswordRequest request);
}
