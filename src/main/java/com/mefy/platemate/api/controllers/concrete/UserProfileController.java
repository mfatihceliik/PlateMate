package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserProfileController;
import com.mefy.platemate.business.abstracts.IUserProfileService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserProfileDto;
import com.mefy.platemate.entities.dto.UserProfilePageDto;
import com.mefy.platemate.entities.dto.request.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserProfileController implements IUserProfileController {

    private final IUserProfileService userProfileService;

    @Override
    public ResponseEntity<DataResult<UserProfileDto>> getByUserId(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long requesterUserId) {
        DataResult<UserProfileDto> result = userProfileService.getByUserId(userId, requesterUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<UserProfilePageDto>> getPageByUserId(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long requesterUserId) {
        DataResult<UserProfilePageDto> result = userProfileService.getPageByUserId(userId, requesterUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> updateProfile(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        Result result = userProfileService.updateProfile(userId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
