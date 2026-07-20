package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.FollowListItemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/follows")
public interface IFollowController {

    @PostMapping("/{userId}")
    ResponseEntity<Result> follow(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId);

    @DeleteMapping("/{userId}")
    ResponseEntity<Result> unfollow(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId);

    @GetMapping("/{userId}/followers")
    ResponseEntity<DataResult<List<FollowListItemDto>>> getFollowers(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId);

    @GetMapping("/{userId}/following")
    ResponseEntity<DataResult<List<FollowListItemDto>>> getFollowing(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long userId);
}
