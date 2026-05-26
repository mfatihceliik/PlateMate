package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.AddCommentReportRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/comments")
public interface ICommentReportController {

    @PostMapping("/{commentId}/reports")
    ResponseEntity<Result> addReport(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddCommentReportRequest request
    );
}
