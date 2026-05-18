package com.mefy.platemate.api.controllers;

import com.mefy.platemate.api.controllers.concrete.AuthController;
import com.mefy.platemate.business.abstracts.IRefreshTokenService;
import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.core.utilities.mappers.UserMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private IUserService userService;
    private IRefreshTokenService refreshTokenService;
    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder passwordEncoder;
    private IMessageService messageService;
    private UserMapper userMapper;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        userService = mock(IUserService.class);
        refreshTokenService = mock(IRefreshTokenService.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        passwordEncoder = mock(PasswordEncoder.class);
        messageService = mock(IMessageService.class);
        userMapper = mock(UserMapper.class);

        authController = new AuthController(
                userService,
                refreshTokenService,
                jwtTokenProvider,
                passwordEncoder,
                messageService,
                userMapper
        );
    }

    @Test
    void registerReturnsAccessAndRefreshTokens() {
        RegisterRequest request = new RegisterRequest("fatih", "password123", "fatih@example.com");
        User user = buildUser(1L, "fatih", "hashed-password");
        UserDto mappedDto = new UserDto();
        mappedDto.setId(1L);
        mappedDto.setUsername("fatih");

        when(userService.add(any(User.class))).thenReturn(new SuccessDataResult<>(user, "created"));
        when(refreshTokenService.issueTokens(user)).thenReturn(new AuthTokensDto("access-token", "refresh-token"));
        when(userMapper.entityToDto(user)).thenReturn(mappedDto);

        ResponseEntity<DataResult<UserDto>> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("access-token", response.getBody().getData().getToken());
        assertEquals("refresh-token", response.getBody().getData().getRefreshToken());
    }

    @Test
    void loginReturnsAccessAndRefreshTokens() {
        LoginRequest request = new LoginRequest("fatih", null, "password123");
        User user = buildUser(2L, "fatih", "encoded");
        UserDto mappedDto = new UserDto();
        mappedDto.setId(2L);
        mappedDto.setUsername("fatih");

        when(userService.getByUsernameOrEmailForAuth("fatih")).thenReturn(new SuccessDataResult<>(user, "found"));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(refreshTokenService.issueTokens(user)).thenReturn(new AuthTokensDto("access-token", "refresh-token"));
        when(userMapper.entityToDto(user)).thenReturn(mappedDto);
        when(messageService.getMessage("auth.login.success")).thenReturn("login-ok");

        ResponseEntity<DataResult<UserDto>> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access-token", response.getBody().getData().getToken());
        assertEquals("refresh-token", response.getBody().getData().getRefreshToken());
    }

    @Test
    void refreshReturnsUnauthorizedWithCodeWhenTokenRevoked() {
        when(refreshTokenService.refreshTokens("revoked-refresh"))
                .thenThrow(new IRefreshTokenService.RefreshTokenServiceException(
                        JwtTokenProvider.RefreshTokenErrorCode.REFRESH_REVOKED
                ));
        when(messageService.getMessage("auth.refresh.revoked")).thenReturn("refresh revoked");

        ResponseEntity<DataResult<UserDto>> response = authController.refresh(new RefreshTokenRequest("revoked-refresh"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Object data = response.getBody().getData();
        assertTrue(data instanceof Map);
        assertEquals("REFRESH_REVOKED", ((Map<?, ?>) data).get("code"));
    }

    @Test
    void refreshReturnsNewAccessAndRefreshTokensOnSuccess() {
        UserDto userDto = new UserDto();
        userDto.setId(9L);
        userDto.setUsername("emine");

        when(refreshTokenService.refreshTokens("valid-refresh"))
                .thenReturn(new AuthTokensDto("new-access", "new-refresh"));
        when(jwtTokenProvider.getUserIdFromToken("new-access")).thenReturn(9L);
        when(userService.getById(9L)).thenReturn(new SuccessDataResult<>(userDto, "user-found"));
        when(messageService.getMessage("auth.refresh.success")).thenReturn("refresh-ok");

        ResponseEntity<DataResult<UserDto>> response = authController.refresh(new RefreshTokenRequest("valid-refresh"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-access", response.getBody().getData().getToken());
        assertEquals("new-refresh", response.getBody().getData().getRefreshToken());
    }

    @Test
    void logoutReturnsSuccessAndIsIdempotent() {
        when(messageService.getMessage("auth.logout.success")).thenReturn("logout-ok");

        ResponseEntity<Result> response = authController.logout(new RefreshTokenRequest("any-refresh"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("logout-ok", response.getBody().getMessage());
    }

    private User buildUser(Long id, String username, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}
