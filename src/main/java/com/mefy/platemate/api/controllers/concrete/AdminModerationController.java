package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminModerationController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IModerationAdminService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateAdminDto;
import com.mefy.platemate.entities.dto.PlateReviewAdminDto;
import com.mefy.platemate.entities.dto.request.AdminCommentModerationRequest;
import com.mefy.platemate.entities.dto.request.HidePlateRequest;
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
public class AdminModerationController implements IAdminModerationController {

    private final IAdminAccessService adminAccessService;
    private final IModerationAdminService moderationAdminService;

    @Override
    public ResponseEntity<DataResult<PagedData<PlateReviewAdminDto>>> getPendingComments(
            @RequestAttribute("userId") Long currentUserId,
            int page,
            int size
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(authResult.getMessage()));
        }
        return ResponseEntity.ok(moderationAdminService.getPendingComments(PaginationRequest.of(page, size)));
    }

    @Override
    public ResponseEntity<Result> approveComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = moderationAdminService.approveComment(commentId, currentUserId);
        if (!result.isSuccess()) return ResponseEntity.badRequest().body(result);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> rejectComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AdminCommentModerationRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = moderationAdminService.rejectComment(commentId, currentUserId, request.getReason());
        if (!result.isSuccess()) return ResponseEntity.badRequest().body(result);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> removeComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AdminCommentModerationRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = moderationAdminService.removeComment(commentId, currentUserId, request.getReason());
        if (!result.isSuccess()) return ResponseEntity.badRequest().body(result);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PagedData<PlateAdminDto>>> getHiddenPlates(
            @RequestAttribute("userId") Long currentUserId,
            int page,
            int size
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorDataResult<>(authResult.getMessage()));
        }
        return ResponseEntity.ok(moderationAdminService.getHiddenPlates(PaginationRequest.of(page, size)));
    }

    @Override
    public ResponseEntity<Result> hidePlate(
            @PathVariable Long plateId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody HidePlateRequest request
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = moderationAdminService.hidePlate(plateId, currentUserId, request.getReason());
        if (!result.isSuccess()) return ResponseEntity.badRequest().body(result);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> restorePlate(
            @PathVariable Long plateId,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = moderationAdminService.restorePlate(plateId, currentUserId);
        if (!result.isSuccess()) return ResponseEntity.badRequest().body(result);
        return ResponseEntity.ok(result);
    }
}
