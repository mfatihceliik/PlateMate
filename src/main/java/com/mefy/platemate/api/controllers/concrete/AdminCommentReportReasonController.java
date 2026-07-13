package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminCommentReportReasonController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.ICommentReportReasonService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.CommentReportReasonAdminDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdateCommentReportReasonActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdateCommentReportReasonRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminCommentReportReasonController implements IAdminCommentReportReasonController {

    private final IAdminAccessService adminAccessService;
    private final ICommentReportReasonService commentReportReasonService;

    @Override
    public ResponseEntity<DataResult<List<CommentReportReasonAdminDto>>> getAll(
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.mefy.platemate.core.utilities.results.ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<List<CommentReportReasonAdminDto>> result = commentReportReasonService.getAllReasons();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<CommentReportReasonAdminDto>> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddCommentReportReasonRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.mefy.platemate.core.utilities.results.ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<CommentReportReasonAdminDto> result = commentReportReasonService.addReason(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<DataResult<CommentReportReasonAdminDto>> update(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateCommentReportReasonRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new com.mefy.platemate.core.utilities.results.ErrorDataResult<>(authResult.getMessage()));
        }

        DataResult<CommentReportReasonAdminDto> result = commentReportReasonService.updateReason(id, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> setActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateCommentReportReasonActiveRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }

        Result result = commentReportReasonService.setReasonActive(id, request.getActive());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
