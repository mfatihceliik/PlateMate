package com.mefy.platemate.api.controllers;

import com.mefy.platemate.api.controllers.concrete.AuthController;
import com.mefy.platemate.business.abstracts.IAuthService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RefreshTokenRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private IAuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(IAuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    void registerSuccessReturnsCreated() {
        RegisterRequest request = new RegisterRequest("fatih", "password123", "fatih@example.com");
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("fatih");
        userDto.setToken("access-token");
        userDto.setRefreshToken("refresh-token");

        when(authService.register(request)).thenReturn(new SuccessDataResult<>(userDto, "created"));

        ResponseEntity<DataResult<UserDto>> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("access-token", response.getBody().getData().getToken());
    }

    @Test
    void registerFailureReturnsBadRequest() {
        RegisterRequest request = new RegisterRequest("fatih", "password123", "fatih@example.com");
        when(authService.register(request)).thenReturn(new ErrorDataResult<>("already exists"));

        ResponseEntity<DataResult<UserDto>> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("already exists", response.getBody().getMessage());
    }

    @Test
    void loginSuccessReturnsOk() {
        LoginRequest request = new LoginRequest("fatih", null, "password123");
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("fatih");
        userDto.setToken("access-token");
        userDto.setRefreshToken("refresh-token");

        when(authService.login(request)).thenReturn(new SuccessDataResult<>(userDto, "login-ok"));

        ResponseEntity<DataResult<UserDto>> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void loginFailureReturnsUnauthorized() {
        LoginRequest request = new LoginRequest("fatih", null, "password123");
        when(authService.login(request)).thenReturn(new ErrorDataResult<>("invalid credentials"));

        ResponseEntity<DataResult<UserDto>> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void refreshSuccessReturnsOk() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh");
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setToken("new-access");
        userDto.setRefreshToken("new-refresh");

        when(authService.refresh(request)).thenReturn(new SuccessDataResult<>(userDto, "refresh-ok"));

        ResponseEntity<DataResult<UserDto>> response = authController.refresh(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-access", response.getBody().getData().getToken());
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshFailureReturnsUnauthorizedWithCode() {
        RefreshTokenRequest request = new RefreshTokenRequest("revoked-refresh");
        DataResult<UserDto> errorResult = (DataResult<UserDto>) (DataResult<?>) new ErrorDataResult<>(
                Map.of("code", "REFRESH_REVOKED"),
                "refresh revoked"
        );

        when(authService.refresh(request)).thenReturn(errorResult);

        ResponseEntity<DataResult<UserDto>> response = authController.refresh(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<?, ?> data = (Map<?, ?>) response.getBody().getData();
        assertEquals("REFRESH_REVOKED", data.get("code"));
    }

    @Test
    void logoutReturnsOk() {
        RefreshTokenRequest request = new RefreshTokenRequest("any-refresh");
        when(authService.logout(request)).thenReturn(new SuccessResult("logout-ok"));

        ResponseEntity<Result> response = authController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("logout-ok", response.getBody().getMessage());
    }
}
