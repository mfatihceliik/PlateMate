package com.mefy.platemate.business.utilities.moderation;

import com.mefy.platemate.dataAccess.abstracts.IPlateReviewModerationEventDao;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewModerationActionType;
import com.mefy.platemate.entities.concrete.PlateReviewModerationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlateReviewModerationEventService {

    private final IPlateReviewModerationEventDao moderationEventDao;

    public void logEvent(
            PlateReview review,
            Long fromStatusId,
            Long toStatusId,
            PlateReviewModerationActionType actionType,
            Long actorUserId,
            String reason
    ) {
        if (review == null || review.getId() == null || toStatusId == null || actionType == null) {
            return;
        }

        PlateReviewModerationEvent event = new PlateReviewModerationEvent();
        event.setPlateReview(review);
        event.setFromStatusId(fromStatusId);
        event.setToStatusId(toStatusId);
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
