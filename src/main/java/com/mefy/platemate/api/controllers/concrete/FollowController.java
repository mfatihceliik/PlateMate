package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IFollowController;
import com.mefy.platemate.business.abstracts.IFollowService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.FollowListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FollowController implements IFollowController {

    private final IFollowService followService;

    @Override
    public ResponseEntity<Result> follow(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId) {
        Result result = followService.follow(currentUserId, userId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> unfollow(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId) {
        Result result = followService.unfollow(currentUserId, userId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<List<FollowListItemDto>>> getFollowers(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId) {
        DataResult<List<FollowListItemDto>> result = followService.getFollowers(userId, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<List<FollowListItemDto>>> getFollowing(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId) {
        DataResult<List<FollowListItemDto>> result = followService.getFollowing(userId, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
