package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IRefreshTokenService;
import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.core.utilities.mappers.UserMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.AuthTokensDto;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RefreshTokenRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthManagerTest {

    private IUserService userService;
    private IRefreshTokenService refreshTokenService;
    private PasswordEncoder passwordEncoder;
    private IMessageService messageService;
    private UserMapper userMapper;
    private JwtTokenProvider jwtTokenProvider;
    private AuthManager authManager;

    @BeforeEach
    void setUp() {
        userService = mock(IUserService.class);
        refreshTokenService = mock(IRefreshTokenService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        messageService = mock(IMessageService.class);
        userMapper = mock(UserMapper.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);

        authManager = new AuthManager(
                userService,
                refreshTokenService,
                passwordEncoder,
                messageService,
                userMapper,
                jwtTokenProvider
        );
    }

    @Test
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("fatih", "password123", "fatih@example.com");
        User user = new User();
        user.setId(1L);
        user.setUsername("fatih");

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("fatih");

        when(userService.register(request)).thenReturn(new SuccessDataResult<>(user, "registered-ok"));
        when(refreshTokenService.issueTokens(user)).thenReturn(new AuthTokensDto("access", "refresh"));
        when(userMapper.entityToDto(user)).thenReturn(userDto);

        DataResult<UserDto> result = authManager.register(request);

        assertTrue(result.isSuccess());
        assertEquals("access", result.getData().getToken());
        assertEquals("refresh", result.getData().getRefreshToken());
    }

    @Test
    void registerFailure() {
        RegisterRequest request = new RegisterRequest("fatih", "password123", "fatih@example.com");
        when(userService.register(request)).thenReturn(new ErrorDataResult<>("username exists"));

        DataResult<UserDto> result = authManager.register(request);

        assertFalse(result.isSuccess());
        assertEquals("username exists", result.getMessage());
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest("fatih", null, "password123");
        User user = new User();
        user.setId(2L);
        user.setUsername("fatih");
        user.setPassword("hashed-password");

        UserDto userDto = new UserDto();
        userDto.setId(2L);
        userDto.setUsername("fatih");

        when(userService.getByUsernameOrEmailForAuth("fatih")).thenReturn(new SuccessDataResult<>(user, "found"));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(refreshTokenService.issueTokens(user)).thenReturn(new AuthTokensDto("access", "refresh"));
        when(userMapper.entityToDto(user)).thenReturn(userDto);
        when(messageService.getMessage("auth.login.success")).thenReturn("login-ok");

        DataResult<UserDto> result = authManager.login(request);

        assertTrue(result.isSuccess());
        assertEquals("access", result.getData().getToken());
        assertEquals("refresh", result.getData().getRefreshToken());
    }

    @Test
    void loginFailedInvalidCredentials() {
        LoginRequest request = new LoginRequest("fatih", null, "password123");
        when(userService.getByUsernameOrEmailForAuth("fatih")).thenReturn(new ErrorDataResult<>("not found"));
        when(messageService.getMessage("auth.invalid.credentials")).thenReturn("invalid");

        DataResult<UserDto> result = authManager.login(request);

        assertFalse(result.isSuccess());
        assertEquals("invalid", result.getMessage());
    }

    @Test
    void refreshSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh");
        UserDto userDto = new UserDto();
        userDto.setId(3L);
        userDto.setUsername("fatih");

        when(refreshTokenService.refreshTokens("valid-refresh")).thenReturn(new AuthTokensDto("new-access", "new-refresh"));
        when(jwtTokenProvider.getUserIdFromToken("new-access")).thenReturn(3L);
        when(userService.getById(3L)).thenReturn(new SuccessDataResult<>(userDto, "found"));
        when(messageService.getMessage("auth.refresh.success")).thenReturn("refresh-ok");

        DataResult<UserDto> result = authManager.refresh(request);

        assertTrue(result.isSuccess());
        assertEquals("new-access", result.getData().getToken());
        assertEquals("new-refresh", result.getData().getRefreshToken());
    }

    @Test
    void refreshFailureException() {
        RefreshTokenRequest request = new RefreshTokenRequest("expired-refresh");
        when(refreshTokenService.refreshTokens("expired-refresh"))
                .thenThrow(new IRefreshTokenService.RefreshTokenServiceException(
                        JwtTokenProvider.RefreshTokenErrorCode.REFRESH_EXPIRED
                ));
        when(messageService.getMessage("auth.refresh.expired")).thenReturn("expired");

        DataResult<UserDto> result = authManager.refresh(request);

        assertFalse(result.isSuccess());
        assertEquals("expired", result.getMessage());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals("REFRESH_EXPIRED", data.get("code"));
    }

    @Test
    void logoutSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest("my-refresh");
        when(messageService.getMessage("auth.logout.success")).thenReturn("logout-ok");

        Result result = authManager.logout(request);

        assertTrue(result.isSuccess());
        assertEquals("logout-ok", result.getMessage());
        verify(refreshTokenService).revoke("my-refresh");
    }
}
