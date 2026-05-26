package com.mefy.platemate.business.utilities.moderation;

import com.mefy.platemate.dataAccess.abstracts.IPlateReviewModerationEventDao;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReviewModerationEvent;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlateReviewModerationEventService {

    private final IPlateReviewModerationEventDao moderationEventDao;

    public void logEvent(
            PlateReview review,
            PlateReviewStatus fromStatus,
            PlateReviewStatus toStatus,
            PlateReviewModerationActionType actionType,
            Long actorUserId,
            String reason
    ) {
        if (review == null || review.getId() == null || toStatus == null || actionType == null) {
            return;
        }

        PlateReviewModerationEvent event = new PlateReviewModerationEvent();
        event.setPlateReview(review);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setActionType(actionType);
        event.setActorUserId(actorUserId);
        event.setReason(normalizeReason(reason));
        event.setCreatedAt(LocalDateTime.now());
        moderationEventDao.save(event);
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
