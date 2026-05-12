package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.ISubscriptionController;
import com.mefy.platemate.business.abstracts.ISubscriptionService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.UserSubscriptionDto;
import com.mefy.platemate.entities.dto.request.ActivateSubscriptionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionController implements ISubscriptionController {

    private final ISubscriptionService subscriptionService;

    @Override
    public ResponseEntity<DataResult<UserDto>> activate(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody ActivateSubscriptionRequest request
    ) {
        DataResult<UserDto> result = subscriptionService.activate(currentUserId, request.getDays());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<UserDto>> getCurrentSubscription(@RequestAttribute("userId") Long currentUserId) {
        DataResult<UserDto> result = subscriptionService.getCurrentSubscription(currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<List<UserSubscriptionDto>>> getMySubscriptionHistory(
            @RequestAttribute("userId") Long currentUserId
    ) {
        DataResult<List<UserSubscriptionDto>> result = subscriptionService.getMySubscriptionHistory(currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
