package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserReviewController;

import com.mefy.platemate.business.abstracts.IUserReviewService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.concrete.UserReview;
import com.mefy.platemate.entities.dto.UserReviewDto;
import com.mefy.platemate.entities.dto.request.AddReviewRequest;
import com.mefy.platemate.entities.dto.request.UpdateReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequiredArgsConstructor
public class UserReviewController implements IUserReviewController {

    private final IUserReviewService userReviewService;

    @Override
    public ResponseEntity<DataResult<Page<UserReviewDto>>> getByProfileId(
            @PathVariable Long profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userReviewService.getByTargetProfileId(profileId, page, size));
    }

    @Override
    public ResponseEntity<Result> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddReviewRequest request
    ) {

        User reviewer = new User();
        reviewer.setId(currentUserId);

        UserProfile targetProfile = new UserProfile();
        targetProfile.setId(request.getTargetProfileId());

        UserReview review = new UserReview();
        review.setReviewer(reviewer);
        review.setTargetProfile(targetProfile);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Result result = userReviewService.add(review);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {

        UserReview review = new UserReview();
        review.setId(request.getId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Result result = userReviewService.update(review, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId) {
        Result result = userReviewService.delete(id, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
