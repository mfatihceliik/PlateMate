package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserLocationsController;
import com.mefy.platemate.business.abstracts.IUserLocationService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserLocationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserLocationsController implements IUserLocationsController {

    private final IUserLocationService userLocationService;

    @Override
    public ResponseEntity<DataResult<UserLocationDto>> getLocationByUserId(@PathVariable Long userId) {
        DataResult<UserLocationDto> result = userLocationService.getLocationByUserId(userId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @Override
    public ResponseEntity<DataResult<List<UserLocationDto>>> getVisibleLocations(Long currentUserId) {
        DataResult<List<UserLocationDto>> result = userLocationService.getVisibleLocationsForUser(currentUserId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> blockUser(Long currentUserId, Long targetUserId) {
        Result result = userLocationService.blockUserFromLocation(currentUserId, targetUserId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> unblockUser(Long currentUserId, Long targetUserId) {
        Result result = userLocationService.unblockUserFromLocation(currentUserId, targetUserId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<List<Long>>> getBlockedUsers(Long currentUserId) {
        return ResponseEntity.ok(userLocationService.getBlockedUserIds(currentUserId));
    }
}
