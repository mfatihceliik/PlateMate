package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAuthController;
import com.mefy.platemate.business.abstracts.IAuthService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.ChangePasswordRequest;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RefreshTokenRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final IAuthService authService;

    @Override
    public ResponseEntity<DataResult<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        DataResult<UserDto> result = authService.register(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<DataResult<UserDto>> login(@Valid @RequestBody LoginRequest request) {
        DataResult<UserDto> result = authService.login(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<UserDto>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        DataResult<UserDto> result = authService.refresh(request);
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> logout(@Valid @RequestBody RefreshTokenRequest request) {
        Result result = authService.logout(request);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> changePassword(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody ChangePasswordRequest request) {
        Result result = authService.changePassword(currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
