package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserLocationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/locations")
public interface IUserLocationsController {

    @GetMapping("/user/{userId}")
    ResponseEntity<DataResult<UserLocationDto>> getLocationByUserId(@PathVariable Long userId);

    @GetMapping("/visible")
    ResponseEntity<DataResult<List<UserLocationDto>>> getVisibleLocations(@RequestAttribute("userId") Long currentUserId);

    @PostMapping("/block/{targetUserId}")
    ResponseEntity<Result> blockUser(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long targetUserId
    );

    @DeleteMapping("/block/{targetUserId}")
    ResponseEntity<Result> unblockUser(
            @RequestAttribute("userId") Long currentUserId,
            @PathVariable Long targetUserId
    );

    @GetMapping("/blocked")
    ResponseEntity<DataResult<List<Long>>> getBlockedUsers(@RequestAttribute("userId") Long currentUserId);
}
