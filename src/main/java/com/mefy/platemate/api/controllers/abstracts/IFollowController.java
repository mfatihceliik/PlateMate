package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
