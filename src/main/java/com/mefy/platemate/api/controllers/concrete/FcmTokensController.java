package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.business.abstracts.IFcmTokenService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.RegisterTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm-tokens")
@RequiredArgsConstructor
public class FcmTokensController {

    private final IFcmTokenService fcmTokenService;

    @PostMapping("/register")
    public ResponseEntity<Result> register(
            @RequestAttribute("userId") Long userId,
            @RequestBody RegisterTokenRequest request
    ) {
        Result result = fcmTokenService.registerToken(userId, request.getToken(), request.getDeviceId());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<Result> unregister(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam String token
    ) {
        Result result = fcmTokenService.unregisterToken(currentUserId, token);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
