package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.business.abstracts.IFcmTokenService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.RegisterTokenRequest;
import com.mefy.platemate.api.controllers.abstracts.IFcmTokensController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FcmTokensController implements IFcmTokensController {

    private final IFcmTokenService fcmTokenService;

    @Override
    public ResponseEntity<Result> register(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody RegisterTokenRequest request
    ) {
        Result result = fcmTokenService.registerToken(userId, request.getToken(), request.getDeviceId());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
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
