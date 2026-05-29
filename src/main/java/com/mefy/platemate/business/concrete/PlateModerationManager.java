package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateModerationService;
import com.mefy.platemate.business.utilities.moderation.ContentModerationResult;
import com.mefy.platemate.business.utilities.moderation.ContentModerationService;
import com.mefy.platemate.business.utilities.moderation.PlateReviewModerationEventService;
import com.mefy.platemate.business.utilities.security.HashingService;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlateModerationManager implements IPlateModerationService {

    private final ContentModerationService contentModerationService;
    private final HashingService hashingService;
    private final PlateReviewModerationEventService moderationEventService;

    @Override
    public ContentModerationResult resolveModeration(User user, String normalizedComment) {
        if (normalizedComment == null || normalizedComment.isBlank()) {
            return new ContentModerationResult(true, false, List.of(), "");
        }

        if (user != null && user.isPremiumActive()) {
            return contentModerationService.moderate(normalizedComment);
        }

        return new ContentModerationResult(false, false, List.of("NON_PREMIUM_TEXT_COMMENT_NOT_ALLOWED"), normalizedComment);
    }

    @Override
    public void applyModerationMetadata(PlateReview review, ContentModerationResult moderation, String responsibilityPolicyVersion) {
        review.setComment(moderation.getSanitizedText());
        review.setStatus(PlateReviewStatus.PENDING_REVIEW);
        review.setModerationReason(moderation.isRequiresReview()
                ? String.join(",", moderation.getReasons())
                : null);
        review.setResponsibilityPolicyVersion(resolveResponsibilityPolicyVersion(responsibilityPolicyVersion));
        
        RequestMetadata requestMetadata = resolveRequestMetadata();
        review.setIpHash(requestMetadata.ipHash());
        review.setUserAgentHash(requestMetadata.userAgentHash());
    }

    @Override
    public void logReviewSubmitted(PlateReview review, Long previousStatusId, Long currentUserId, String actionReason) {
        moderationEventService.logEvent(
                review,
                previousStatusId,
                review.getStatusId(),
                PlateReviewModerationActionType.SUBMITTED_FOR_REVIEW,
                currentUserId,
                actionReason
        );
    }

    @Override
    public void logReviewRemovedByUser(PlateReview review, Long previousStatusId, Long currentUserId) {
        moderationEventService.logEvent(
                review,
                previousStatusId,
                review.getStatusId(),
                PlateReviewModerationActionType.REMOVED_BY_USER,
                currentUserId,
                "USER_REMOVED_REVIEW"
        );
    }

    private String resolveResponsibilityPolicyVersion(String responsibilityPolicyVersion) {
        if (responsibilityPolicyVersion == null || responsibilityPolicyVersion.isBlank()) {
            return "v1";
        }
        return responsibilityPolicyVersion.trim();
    }

    private RequestMetadata resolveRequestMetadata() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return new RequestMetadata(null, null);
        }
        HttpServletRequest request = requestAttributes.getRequest();
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return new RequestMetadata(hashingService.sha256(clientIp), hashingService.sha256(userAgent));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record RequestMetadata(String ipHash, String userAgentHash) {
    }
}
