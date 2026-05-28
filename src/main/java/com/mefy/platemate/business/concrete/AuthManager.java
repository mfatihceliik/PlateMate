package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAuthService;
import com.mefy.platemate.business.abstracts.IRefreshTokenService;
import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.core.utilities.mappers.UserMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.AuthTokensDto;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RefreshTokenRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthManager implements IAuthService {

    private final IUserService userService;
    private final IRefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final IMessageService messageService;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public DataResult<UserDto> register(RegisterRequest request) {
        DataResult<User> result = userService.register(request);
        if (!result.isSuccess()) {
            return new ErrorDataResult<>(result.getMessage());
        }

        AuthTokensDto authTokens = refreshTokenService.issueTokens(result.getData());
        UserDto userDto = userMapper.entityToDto(result.getData());
        attachTokens(userDto, authTokens);

        return new SuccessDataResult<>(userDto, result.getMessage());
    }

    @Override
    public DataResult<UserDto> login(LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier == null || identifier.trim().isEmpty()) {
            return new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials"));
        }

        DataResult<User> result = userService.getByUsernameOrEmailForAuth(identifier);
        if (!result.isSuccess()) {
            return new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials"));
        }

        User user = result.getData();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials"));
        }

        AuthTokensDto authTokens = refreshTokenService.issueTokens(user);
        UserDto userDto = userMapper.entityToDto(user);
        attachTokens(userDto, authTokens);

        return new SuccessDataResult<>(userDto, messageService.getMessage("auth.login.success"));
    }

    @Override
    public DataResult<UserDto> refresh(RefreshTokenRequest request) {
        try {
            AuthTokensDto authTokens = refreshTokenService.refreshTokens(request.getRefreshToken());
            Long userId = jwtTokenProvider.getUserIdFromToken(authTokens.getAccessToken());

            DataResult<UserDto> userResult = userService.getById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return buildRefreshError(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_INVALID);
            }

            UserDto userDto = userResult.getData();
            attachTokens(userDto, authTokens);

            return new SuccessDataResult<>(userDto, messageService.getMessage("auth.refresh.success"));
        } catch (IRefreshTokenService.RefreshTokenServiceException e) {
            return buildRefreshError(e.getCode());
        }
    }

    @Override
    public Result logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return new SuccessResult(messageService.getMessage("auth.logout.success"));
    }

    private void attachTokens(UserDto userDto, AuthTokensDto authTokens) {
        userDto.setToken(authTokens.getAccessToken());
        userDto.setRefreshToken(authTokens.getRefreshToken());
    }

    @SuppressWarnings("unchecked")
    private DataResult<UserDto> buildRefreshError(JwtTokenProvider.RefreshTokenErrorCode code) {
        String messageKey = switch (code) {
            case REFRESH_EXPIRED -> "auth.refresh.expired";
            case REFRESH_REVOKED -> "auth.refresh.revoked";
            case REFRESH_INVALID -> "auth.refresh.invalid";
        };

        return (DataResult<UserDto>) (DataResult<?>) new ErrorDataResult<>(
                Map.of("code", code.name()),
                messageService.getMessage(messageKey)
        );
    }
}
