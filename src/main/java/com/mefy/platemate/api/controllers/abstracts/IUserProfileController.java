package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.request.UpdateProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

@RequestMapping("/api/profiles")
public interface IUserProfileController {

    @GetMapping("/{userId}")
    ResponseEntity<DataResult<UserProfileDto>> getByUserId(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long requesterUserId);

    @PutMapping("/{userId}")
    ResponseEntity<Result> updateProfile(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateProfileRequest request);
}
