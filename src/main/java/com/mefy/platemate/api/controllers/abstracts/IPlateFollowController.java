package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/plates")
public interface IPlateFollowController {

    @PostMapping("/{plateCode}/follow")
    ResponseEntity<Result> followPlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId);

    @DeleteMapping("/{plateCode}/follow")
    ResponseEntity<Result> unfollowPlate(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId);
}
