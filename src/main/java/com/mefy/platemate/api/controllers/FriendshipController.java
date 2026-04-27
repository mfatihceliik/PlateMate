package com.mefy.platemate.api.controllers;

import com.mefy.platemate.business.abstracts.IFriendshipService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.FriendshipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final IFriendshipService friendshipService;

    @PostMapping("/request/{addresseeId}")
    public ResponseEntity<Result> sendRequest(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long addresseeId) {
        Result result = friendshipService.sendRequest(currentUserId, addresseeId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Result> accept(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = friendshipService.acceptRequest(id, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Result> reject(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = friendshipService.rejectRequest(id, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result> remove(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = friendshipService.removeFriend(id, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<DataResult<List<FriendshipDto>>> getFriends(
            @RequestAttribute("userId") Long currentUserId) {
        return ResponseEntity.ok(friendshipService.getFriends(currentUserId));
    }

    @GetMapping("/pending")
    public ResponseEntity<DataResult<List<FriendshipDto>>> getPendingRequests(
            @RequestAttribute("userId") Long currentUserId) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(currentUserId));
    }
}
