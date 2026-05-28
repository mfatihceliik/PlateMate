package com.mefy.platemate.business.discovery;

import com.mefy.platemate.business.utilities.time.TimeWindow;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.entities.concrete.PlateStatus;
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
        return buildRecentActivities(activityLimit, null);
    }

    public List<DiscoveryRecentActivityDto> buildRecentActivities(int activityLimit, TimeWindow window) {
        int sourceLimit = Math.max(activityLimit * 2, activityLimit);
        List<DiscoveryRecentActivityDto> activities = new ArrayList<>();

        var pageable = PageRequest.of(0, sourceLimit);
        var recentReviews = window == null
                ? plateReviewDao.getRecentReviewActivities(pageable)
                : plateReviewDao.getRecentReviewActivitiesByWindow(window.getStart(), window.getEnd(), pageable);

        recentReviews.forEach(review -> {
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

        var recentReports = window == null
                ? plateReportDao.getRecentReportActivitiesAndPlateStatus(PlateStatus.ACTIVE.getId(), pageable)
                : plateReportDao.getRecentReportActivitiesByWindowAndPlateStatus(window.getStart(), window.getEnd(), PlateStatus.ACTIVE.getId(), pageable);

        recentReports.forEach(report -> {
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
