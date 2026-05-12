package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.UserSubscriptionDto;
import com.mefy.platemate.entities.dto.request.ActivateSubscriptionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/subscriptions")
public interface ISubscriptionController {
    @PostMapping("/activate")
    ResponseEntity<DataResult<UserDto>> activate(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody ActivateSubscriptionRequest request
    );

    @GetMapping("/me")
    ResponseEntity<DataResult<UserDto>> getCurrentSubscription(
            @RequestAttribute("userId") Long currentUserId
    );

    @GetMapping("/me/history")
    ResponseEntity<DataResult<List<UserSubscriptionDto>>> getMySubscriptionHistory(
            @RequestAttribute("userId") Long currentUserId
    );
}
