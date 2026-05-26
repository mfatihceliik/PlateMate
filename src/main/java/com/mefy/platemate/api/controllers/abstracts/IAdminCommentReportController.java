package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.CommentReportDto;
import com.mefy.platemate.entities.dto.request.ReviewCommentReportRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/admin/comment-reports")
public interface IAdminCommentReportController {

    @GetMapping
    ResponseEntity<DataResult<PagedData<CommentReportDto>>> getReports(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @PatchMapping("/{reportId}/review")
    ResponseEntity<Result> reviewReport(
            @PathVariable Long reportId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody ReviewCommentReportRequest request
    );
}
