package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserReviewService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.UserReviewMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.dataAccess.abstracts.IUserReviewDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.concrete.UserReview;
import com.mefy.platemate.entities.dto.UserReviewDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReviewManager implements IUserReviewService {

    private final IUserReviewDao userReviewDao;
    private final IUserProfileDao userProfileDao;
    private final IUserDao userDao;
    private final UserReviewMapper userReviewMapper;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result add(UserReview userReview) {
        Long reviewerId = userReview.getReviewer().getId();
        Long targetId = userReview.getTargetProfile().getId();

        Result result = BusinessRules.run(
                checkIfSelfReview(reviewerId, targetId),
                checkIfUserExistsById(reviewerId),
                checkIfProfileExistsById(targetId)
        );
        if (result != null) return result;

        User reviewer = userDao.findById(reviewerId).orElse(null);
        UserProfile targetProfile = userProfileDao.findById(targetId).orElse(null);

        userReview.setReviewer(reviewer);
        userReview.setTargetProfile(targetProfile);

        userReviewDao.save(userReview);
        updateUserProfileStatistics(targetId, userReview.getRating());

        return new SuccessResult(messageService.getMessage(Messages.REVIEW_ADDED));
    }

    @Override
    public DataResult<Page<UserReviewDto>> getByTargetProfileId(Long targetProfileId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserReview> reviewsPage = userReviewDao.findByTargetProfileId(targetProfileId, pageable);

        Page<UserReviewDto> dtoPage = reviewsPage.map(userReviewMapper::entityToDto);

        return new SuccessDataResult<>(dtoPage, messageService.getMessage(Messages.REVIEWS_LISTED));
    }

    @Override
    @Transactional
    public Result update(UserReview userReview, Long currentUserId) {
        UserReview existingReview = userReviewDao.findById(userReview.getId()).orElse(null);
        
        Result result = BusinessRules.run(
                checkIfReviewExists(existingReview),
                checkIfUserAuthorizedForReview(existingReview, currentUserId)
        );
        if (result != null) return result;

        Integer oldRating = existingReview.getRating();
        Integer newRating = userReview.getRating();

        existingReview.setRating(newRating);
        existingReview.setComment(userReview.getComment());

        userReviewDao.save(existingReview);

        if (!oldRating.equals(newRating)) {
            updateUserProfileStatisticsForEdit(existingReview.getTargetProfile().getId(), oldRating, newRating);
        }

        return new SuccessResult(messageService.getMessage(Messages.REVIEW_UPDATED));
    }

    @Override
    @Transactional
    public Result delete(Long reviewId, Long currentUserId) {
        UserReview review = userReviewDao.findById(reviewId).orElse(null);
        
        Result result = BusinessRules.run(
                checkIfReviewExists(review),
                checkIfUserAuthorizedForReview(review, currentUserId)
        );
        if (result != null) return result;

        rollbackUserProfileStatistics(review.getTargetProfile().getId(), review.getRating());

        userReviewDao.delete(review);
        return new SuccessResult(messageService.getMessage("review.deleted"));
    }

    /// ----- BUSINESS RULES -----

    private void updateUserProfileStatistics(Long profileId, Integer newRating) {
        UserProfile profile = userProfileDao.findById(profileId).orElseThrow();
        int newReviewCount = profile.getReviewCount() + 1;
        long newTotalSum = profile.getTotalRatingSum() + newRating;
        double newAverage = (double) newTotalSum / newReviewCount;
        profile.setReviewCount(newReviewCount);
        profile.setTotalRatingSum(newTotalSum);
        profile.setDriverRating(newAverage);
        userProfileDao.save(profile);
    }

    private void rollbackUserProfileStatistics(Long profileId, Integer removedRating) {
        UserProfile profile = userProfileDao.findById(profileId).orElseThrow();
        int newReviewCount = Math.max(0, profile.getReviewCount() - 1);
        long newTotalSum = Math.max(0, profile.getTotalRatingSum() - removedRating);
        double newAverage = newReviewCount > 0 ? (double) newTotalSum / newReviewCount : 0.0;
        profile.setReviewCount(newReviewCount);
        profile.setTotalRatingSum(newTotalSum);
        profile.setDriverRating(newAverage);
        userProfileDao.save(profile);
    }

    private void updateUserProfileStatisticsForEdit(Long profileId, Integer oldRating, Integer newRating) {
        UserProfile profile = userProfileDao.findById(profileId).orElseThrow();
        long newTotalSum = profile.getTotalRatingSum() - oldRating + newRating;
        double newAverage = profile.getReviewCount() > 0 ? (double) newTotalSum / profile.getReviewCount() : 0.0;
        profile.setTotalRatingSum(newTotalSum);
        profile.setDriverRating(newAverage);
        userProfileDao.save(profile);
    }

    private Result checkIfSelfReview(Long reviewerId, Long targetId) {
        if (reviewerId.equals(targetId)) {
            return new ErrorResult(messageService.getMessage(Messages.SELF_REVIEW_NOT_ALLOWED));
        }
        return new SuccessResult();
    }

    private Result checkIfUserExistsById(Long userId) {
        if (!userDao.existsById(userId)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfProfileExistsById(Long profileId) {
        if (!userProfileDao.existsById(profileId)) {
            return new ErrorResult(messageService.getMessage(Messages.PROFILE_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfReviewExists(UserReview review) {
        if (review == null) {
            return new ErrorResult(messageService.getMessage(Messages.REVIEW_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfUserAuthorizedForReview(UserReview review, Long currentUserId) {
        if (review == null) return new SuccessResult();
        if (!review.getReviewer().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage("review.delete.unauthorized"));
        }
        return new SuccessResult();
    }
}
