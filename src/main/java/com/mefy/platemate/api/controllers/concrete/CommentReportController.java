package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.ICommentReportController;
import com.mefy.platemate.business.abstracts.ICommentReportService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.AddCommentReportRequest;
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
public class CommentReportController implements ICommentReportController {

    private final ICommentReportService commentReportService;

    @Override
    public ResponseEntity<Result> addReport(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddCommentReportRequest request
    ) {
        Result result = commentReportService.addReport(commentId, currentUserId, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
