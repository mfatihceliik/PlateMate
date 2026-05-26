package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateAdminDto;
import com.mefy.platemate.entities.dto.PlateReviewAdminDto;
import com.mefy.platemate.entities.dto.request.AdminCommentModerationRequest;
import com.mefy.platemate.entities.dto.request.HidePlateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/admin")
public interface IAdminModerationController {

    @GetMapping("/comments/pending")
    ResponseEntity<DataResult<PagedData<PlateReviewAdminDto>>> getPendingComments(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @PatchMapping("/comments/{commentId}/approve")
    ResponseEntity<Result> approveComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId
    );

    @PatchMapping("/comments/{commentId}/reject")
    ResponseEntity<Result> rejectComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AdminCommentModerationRequest request
    );

    @PatchMapping("/comments/{commentId}/remove")
    ResponseEntity<Result> removeComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AdminCommentModerationRequest request
    );

    @GetMapping("/plates/hidden")
    ResponseEntity<DataResult<PagedData<PlateAdminDto>>> getHiddenPlates(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @PatchMapping("/plates/{plateId}/hide")
    ResponseEntity<Result> hidePlate(
            @PathVariable Long plateId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody HidePlateRequest request
    );

    @PatchMapping("/plates/{plateId}/restore")
    ResponseEntity<Result> restorePlate(
            @PathVariable Long plateId,
            @RequestAttribute("userId") Long currentUserId
    );
}
