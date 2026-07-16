package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.ReviewResponseDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.SyncPlateReportsRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/plates")
public interface IPlateController {

    @GetMapping("/search")
    ResponseEntity<DataResult<PlateDetailDto>> search(
            @RequestParam String plate,
            @RequestAttribute("userId") Long currentUserId
    );

    @GetMapping("/{plateCode}/reviews")
    ResponseEntity<DataResult<PagedData<PlateReviewDto>>> getReviews(
            @PathVariable String plateCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @PostMapping("/{plateCode}/reviews")
    ResponseEntity<DataResult<ReviewResponseDto>> addReview(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddPlateReviewRequest request
    );

    @GetMapping("/reviews/{id}")
    ResponseEntity<DataResult<PlateReviewDto>> getReviewById(
            @PathVariable Long id
    );

    @GetMapping("/reviews/mine")
    ResponseEntity<DataResult<PagedData<PlateReviewDto>>> getMyReviews(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @PutMapping("/reviews/{id}")
    ResponseEntity<Result> updateReview(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePlateReviewRequest request
    );

    @DeleteMapping("/reviews/{id}")
    ResponseEntity<Result> deleteReview(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );

    @PutMapping("/{plateCode}/reports")
    ResponseEntity<Result> syncReports(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody SyncPlateReportsRequest request
    );
}
