package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IPlateFollowController;
import com.mefy.platemate.business.abstracts.IPlateFollowService;
import com.mefy.platemate.core.utilities.results.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlateFollowController implements IPlateFollowController {

    private final IPlateFollowService plateFollowService;

    @Override
    public ResponseEntity<Result> followPlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = plateFollowService.followPlate(currentUserId, plateCode);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> unfollowPlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = plateFollowService.unfollowPlate(currentUserId, plateCode);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
