package com.mefy.platemate.business.discovery;

import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.entities.dto.DiscoveryActivityActionType;
import com.mefy.platemate.entities.dto.DiscoveryRecentActivityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscoveryActivityService {

    private final IPlateReviewDao plateReviewDao;
    private final IPlateReportDao plateReportDao;

    public List<DiscoveryRecentActivityDto> buildRecentActivities(int activityLimit) {
        int sourceLimit = Math.max(activityLimit * 2, activityLimit);
        List<DiscoveryRecentActivityDto> activities = new ArrayList<>();

        plateReviewDao.getRecentReviewActivities(PageRequest.of(0, sourceLimit)).forEach(review -> {
            DiscoveryActivityActionType actionType = isCreatedAndNotUpdated(review.getCreatedAt(), review.getUpdatedAt())
                    ? DiscoveryActivityActionType.REVIEW_ADDED
                    : DiscoveryActivityActionType.RATING_GIVEN;

            activities.add(new DiscoveryRecentActivityDto(
                    review.getUsername(),
                    review.getPlateCode(),
                    actionType,
                    review.getUpdatedAt() == null ? review.getCreatedAt() : review.getUpdatedAt(),
                    review.getRating(),
                    review.getComment(),
                    null,
                    null
            ));
        });

        plateReportDao.getRecentReportActivities(PageRequest.of(0, sourceLimit)).forEach(report -> {
            activities.add(new DiscoveryRecentActivityDto(
                    report.getUsername(),
                    report.getPlateCode(),
                    DiscoveryActivityActionType.REPORT_SUBMITTED,
                    report.getOccurredAt(),
                    null,
                    null,
                    report.getReportTypeCode(),
                    report.getReportTypeLabel()
            ));
        });

        activities.sort(Comparator.comparing(DiscoveryRecentActivityDto::getOccurredAt, Comparator.nullsLast(Comparator.reverseOrder())));
        if (activities.size() <= activityLimit) {
            return activities;
        }
        return new ArrayList<>(activities.subList(0, activityLimit));
    }

    private boolean isCreatedAndNotUpdated(LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (createdAt == null) return false;
        if (updatedAt == null) return true;
        return createdAt.equals(updatedAt);
    }
}
