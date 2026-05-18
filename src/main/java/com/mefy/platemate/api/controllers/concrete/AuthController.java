package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAuthController;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final IUserService userService;
    private final IRefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final IMessageService messageService;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<DataResult<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        DataResult<User> result = userService.add(user);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(new ErrorDataResult<>(result.getMessage()));
        }

        AuthTokensDto authTokens = refreshTokenService.issueTokens(result.getData());
        UserDto userDto = userMapper.entityToDto(result.getData());
        attachTokens(userDto, authTokens);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(userDto, result.getMessage()));
    }

    @Override
    public ResponseEntity<DataResult<UserDto>> login(@Valid @RequestBody LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier == null || identifier.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials")));
        }

        DataResult<User> result = userService.getByUsernameOrEmailForAuth(identifier);
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials")));
        }

        User user = result.getData();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials")));
        }

        AuthTokensDto authTokens = refreshTokenService.issueTokens(user);
        UserDto userDto = userMapper.entityToDto(user);
        attachTokens(userDto, authTokens);

        return ResponseEntity.ok(new SuccessDataResult<>(userDto, messageService.getMessage("auth.login.success")));
    }

    @Override
    public ResponseEntity<DataResult<UserDto>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthTokensDto authTokens = refreshTokenService.refreshTokens(request.getRefreshToken());
            Long userId = jwtTokenProvider.getUserIdFromToken(authTokens.getAccessToken());

            DataResult<UserDto> userResult = userService.getById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return refreshErrorResponse(JwtTokenProvider.RefreshTokenErrorCode.REFRESH_INVALID);
            }

            UserDto userDto = userResult.getData();
            attachTokens(userDto, authTokens);

            return ResponseEntity.ok(new SuccessDataResult<>(userDto, messageService.getMessage("auth.refresh.success")));
        } catch (IRefreshTokenService.RefreshTokenServiceException e) {
            return refreshErrorResponse(e.getCode());
        }
    }

    @Override
    public ResponseEntity<Result> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(new SuccessResult(messageService.getMessage("auth.logout.success")));
    }

    private void attachTokens(UserDto userDto, AuthTokensDto authTokens) {
        userDto.setToken(authTokens.getAccessToken());
        userDto.setRefreshToken(authTokens.getRefreshToken());
    }

    private ResponseEntity<DataResult<UserDto>> refreshErrorResponse(JwtTokenProvider.RefreshTokenErrorCode code) {
        String messageKey = switch (code) {
            case REFRESH_EXPIRED -> "auth.refresh.expired";
            case REFRESH_REVOKED -> "auth.refresh.revoked";
            case REFRESH_INVALID -> "auth.refresh.invalid";
        };

        @SuppressWarnings("unchecked")
        DataResult<UserDto> errorResult = (DataResult<UserDto>) (DataResult<?>) new ErrorDataResult<>(
                Map.of("code", code.name()),
                messageService.getMessage(messageKey)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResult);
    }
}
