package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.RegisterTokenRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/fcm-tokens")
public interface IFcmTokensController {

    @PostMapping("/register")
    ResponseEntity<Result> register(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody RegisterTokenRequest request
    );

    @DeleteMapping("/unregister")
    ResponseEntity<Result> unregister(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam String token
    );
}
