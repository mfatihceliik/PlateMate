package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.entities.dto.request.LoginRequest;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface IAuthController {

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginRequest request);
}
