package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserBlockController;
import com.mefy.platemate.business.abstracts.IUserBlockService;
import com.mefy.platemate.core.utilities.results.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserBlockController implements IUserBlockController {

    private final IUserBlockService userBlockService;

    @Override
    public ResponseEntity<Result> blockUser(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = userBlockService.blockUser(currentUserId, userId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> unblockUser(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = userBlockService.unblockUser(currentUserId, userId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
