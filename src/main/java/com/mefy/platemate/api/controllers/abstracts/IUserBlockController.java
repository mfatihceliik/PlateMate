package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/users")
public interface IUserBlockController {

    @PostMapping("/{userId}/block")
    ResponseEntity<Result> blockUser(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId);

    @DeleteMapping("/{userId}/block")
    ResponseEntity<Result> unblockUser(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId);
}
