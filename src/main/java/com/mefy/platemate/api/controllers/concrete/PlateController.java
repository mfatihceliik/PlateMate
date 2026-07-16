package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IPlateController;
import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.abstracts.IPlateReviewService;
import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import com.mefy.platemate.entities.dto.ReviewResponseDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import com.mefy.platemate.entities.dto.request.SyncPlateReportsRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PlateController implements IPlateController {

    private final IPlateReviewService plateReviewService;
    private final IPlateSearchService plateSearchService;
    private final IPlateReportService plateReportService;

    @Override
    public ResponseEntity<DataResult<PlateDetailDto>> search(
            @RequestParam String plate,
            @RequestAttribute("userId") Long currentUserId
    ) {
        DataResult<PlateDetailDto> result = plateSearchService.searchByPlateCode(plate, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PagedData<PlateReviewDto>>> getReviews(
            @PathVariable String plateCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaginationRequest paginationRequest = PaginationRequest.of(page, size);
        DataResult<PagedData<PlateReviewDto>> result = plateReviewService.getReviewsByPlateCode(plateCode, paginationRequest);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PlateReviewDto>> getReviewById(
            @PathVariable Long id
    ) {
        DataResult<PlateReviewDto> result = plateReviewService.getReviewById(id);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PagedData<PlateReviewDto>>> getMyReviews(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaginationRequest paginationRequest = PaginationRequest.of(page, size);
        DataResult<PagedData<PlateReviewDto>> result = plateReviewService.getMyReviews(currentUserId, status, query, paginationRequest);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<ReviewResponseDto>> addReview(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddPlateReviewRequest request
    ) {
        DataResult<ReviewResponseDto> result = plateReviewService.addReview(plateCode, currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> updateReview(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePlateReviewRequest request
    ) {
        Result result = plateReviewService.updateReview(id, currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> deleteReview(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result result = plateReviewService.deleteReview(id, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> syncReports(
            @PathVariable String plateCode,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody SyncPlateReportsRequest request
    ) {
        Result result = plateReportService.syncReports(plateCode, currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
