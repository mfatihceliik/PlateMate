package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAuthController;

import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.core.utilities.mappers.UserMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final IUserService userService;
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

        String token = jwtTokenProvider.generateToken(result.getData().getId(), result.getData().getUsername());
        UserDto userDto = userMapper.entityToDto(result.getData());
        userDto.setToken(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessDataResult<>(userDto, result.getMessage())
        );
    }

    @Override
    public ResponseEntity<DataResult<UserDto>> login(@Valid @RequestBody LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier == null || identifier.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials")));
        }

        DataResult<User> result = userService.getByUsernameOrEmailForAuth(identifier);
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials")));
        }

        User user = result.getData();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDataResult<>(messageService.getMessage("auth.invalid.credentials")));
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        UserDto userDto = userMapper.entityToDto(user);
        userDto.setToken(token);

        return ResponseEntity.ok(new SuccessDataResult<>(userDto, messageService.getMessage("auth.login.success")));
    }
}
