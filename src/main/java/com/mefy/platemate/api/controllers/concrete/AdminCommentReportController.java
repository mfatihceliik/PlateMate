package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminCommentReportController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.ICommentReportService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.CommentReportDto;
import com.mefy.platemate.entities.dto.request.ReviewCommentReportRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminCommentReportController implements IAdminCommentReportController {

    private final IAdminAccessService adminAccessService;
    private final ICommentReportService commentReportService;

    @Override
    public ResponseEntity<DataResult<PagedData<CommentReportDto>>> getReports(
            @RequestAttribute("userId") Long currentUserId,
            int page,
            int size
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<PagedData<CommentReportDto>> result = commentReportService.getReports(PaginationRequest.of(page, size));
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> reviewReport(
            @PathVariable Long reportId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody ReviewCommentReportRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = commentReportService.reviewReport(reportId, currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
