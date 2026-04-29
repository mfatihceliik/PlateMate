package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserReviewDto;
import com.mefy.platemate.entities.dto.request.AddReviewRequest;
import com.mefy.platemate.entities.dto.request.UpdateReviewRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/reviews")
public interface IUserReviewController {

    @GetMapping("/{profileId}")
    ResponseEntity<DataResult<Page<UserReviewDto>>> getByProfileId(
            @PathVariable Long profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @PostMapping
    ResponseEntity<Result> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddReviewRequest request
    );

    @PutMapping
    ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateReviewRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );
}
