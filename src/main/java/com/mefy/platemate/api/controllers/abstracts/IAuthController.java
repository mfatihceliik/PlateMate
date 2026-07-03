package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.ChangePasswordRequest;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RefreshTokenRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface IAuthController {

    @PostMapping("/register")
    ResponseEntity<DataResult<UserDto>> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/login")
    ResponseEntity<DataResult<UserDto>> login(@Valid @RequestBody LoginRequest request);

    @PostMapping("/refresh")
    ResponseEntity<DataResult<UserDto>> refresh(@Valid @RequestBody RefreshTokenRequest request);

    @PostMapping("/logout")
    ResponseEntity<Result> logout(@Valid @RequestBody RefreshTokenRequest request);

    @PutMapping("/change-password")
    ResponseEntity<Result> changePassword(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody ChangePasswordRequest request);
}
