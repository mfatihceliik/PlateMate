package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.business.utilities.moderation.ContentModerationResult;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.User;

public interface IPlateModerationService {

    /**
     * Resolves moderation rules for a given user and comment.
     * @param user The user submitting the comment
     * @param normalizedComment The comment to be moderated
     * @return ContentModerationResult indicating if it's allowed, needs review, and sanitized text
     */
    ContentModerationResult resolveModeration(User user, String normalizedComment);

    /**
     * Applies moderation outcomes, request metadata (IP/UserAgent hashes), and sets status to PENDING_REVIEW.
     * @param review The review to mutate
     * @param moderation The moderation result
     * @param responsibilityPolicyVersion The policy version accepted by the user
     */
    void applyModerationMetadata(PlateReview review, ContentModerationResult moderation, String responsibilityPolicyVersion);

    /**
     * Logs the submission or update of a review by a user.
     */
    void logReviewSubmitted(PlateReview review, Long previousStatusId, Long currentUserId, String actionReason);

    /**
     * Logs the removal of a review by the user themselves.
     */
    void logReviewRemovedByUser(PlateReview review, Long previousStatusId, Long currentUserId);
}
