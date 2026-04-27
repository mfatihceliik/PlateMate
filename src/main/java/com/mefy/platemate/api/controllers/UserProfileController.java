package com.mefy.platemate.api.controllers;

import com.mefy.platemate.business.abstracts.IUserProfileService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.request.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final IUserProfileService userProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<DataResult<UserProfileDto>> getByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        DataResult<UserProfileDto> result = userProfileService.getByUserId(userId, page, size);
        if (!result.isSuccess()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping
    public ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfile profile = new UserProfile();
        profile.setId(currentUserId); // Kendi profilini günceller
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setBio(request.getBio());

        Result result = userProfileService.update(profile);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
