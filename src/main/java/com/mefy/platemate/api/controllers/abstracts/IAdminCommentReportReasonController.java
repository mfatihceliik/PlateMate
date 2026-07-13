package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.CommentReportReasonAdminDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdateCommentReportReasonActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdateCommentReportReasonRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/admin/comment-report-reasons")
public interface IAdminCommentReportReasonController {

    @GetMapping
    ResponseEntity<DataResult<List<CommentReportReasonAdminDto>>> getAll(
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping
    ResponseEntity<DataResult<CommentReportReasonAdminDto>> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddCommentReportReasonRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<DataResult<CommentReportReasonAdminDto>> update(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateCommentReportReasonRequest request
    );

    @PatchMapping("/{id}/active")
    ResponseEntity<Result> setActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateCommentReportReasonActiveRequest request
    );
}
