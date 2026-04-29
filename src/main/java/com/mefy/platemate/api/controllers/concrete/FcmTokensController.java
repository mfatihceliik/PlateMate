package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.business.abstracts.IFcmTokenService;
import com.mefy.platemate.core.utilities.results.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm-tokens")
@RequiredArgsConstructor
public class FcmTokensController {

    private final IFcmTokenService fcmTokenService;

    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody RegisterTokenRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(fcmTokenService.registerToken(userId, request.getToken(), request.getDeviceId()));
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<Result> unregister(@RequestParam String token) {
        return ResponseEntity.ok(fcmTokenService.unregisterToken(token));
    }

    @Data
    public static class RegisterTokenRequest {
        private String token;
        private String deviceId;
    }
}
