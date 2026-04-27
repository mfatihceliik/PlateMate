package com.mefy.platemate.api.controllers;

import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final IMessageService messageService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        DataResult<User> result = userService.add(user);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(new ErrorResult(result.getMessage()));
        }

        String token = jwtTokenProvider.generateToken(result.getData().getId(), result.getData().getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SuccessDataResult<>(Map.of("token", token), result.getMessage())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier == null || identifier.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResult(messageService.getMessage("auth.invalid.credentials")));
        }

        DataResult<User> result = userService.getByUsernameOrEmailForAuth(identifier);
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResult(messageService.getMessage("auth.invalid.credentials")));
        }

        User user = result.getData();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResult(messageService.getMessage("auth.invalid.credentials")));
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new SuccessDataResult<>(Map.of("token", token), messageService.getMessage("auth.login.success")));
    }
}
