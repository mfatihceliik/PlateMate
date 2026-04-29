package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.FriendshipDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/friendships")
public interface IFriendshipController {

    @PostMapping("/request/{addresseeId}")
    ResponseEntity<Result> sendRequest(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long addresseeId
    );

    @PutMapping("/{id}/accept")
    ResponseEntity<Result> accept(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );

    @PutMapping("/{id}/reject")
    ResponseEntity<Result> reject(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Result> remove(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );

    @GetMapping
    ResponseEntity<DataResult<List<FriendshipDto>>> getFriends(
            @RequestAttribute("userId") Long currentUserId
    );

    @GetMapping("/pending")
    ResponseEntity<DataResult<List<FriendshipDto>>> getPendingRequests(
            @RequestAttribute("userId") Long currentUserId
    );
}
