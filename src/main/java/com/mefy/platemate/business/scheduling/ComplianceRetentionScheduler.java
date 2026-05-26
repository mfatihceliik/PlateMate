package com.mefy.platemate.business.scheduling;

import com.mefy.platemate.business.utilities.time.TimeWindowService;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateRemovalRequestDao;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplianceRetentionScheduler {

    private static final List<CommentReportStatus> RESOLVED_COMMENT_REPORT_STATUSES = List.of(
            CommentReportStatus.REVIEWED,
            CommentReportStatus.ACCEPTED,
            CommentReportStatus.REJECTED
    );

    private static final List<PlateRemovalRequestStatus> RESOLVED_PLATE_REQUEST_STATUSES = List.of(
            PlateRemovalRequestStatus.ACCEPTED,
            PlateRemovalRequestStatus.REJECTED
    );

    private final ICommentReportDao commentReportDao;
    private final IPlateRemovalRequestDao plateRemovalRequestDao;

    @Value("${platemate.moderation-retention-days:365}")
    private int moderationRetentionDays = 365;

    @Scheduled(
            cron = "${platemate.compliance-retention.cron:0 45 3 * * *}",
            zone = "Europe/Istanbul"
    )
    @Transactional
    public void cleanupExpiredComplianceData() {
        LocalDateTime now = LocalDateTime.now(TimeWindowService.TURKEY_ZONE);

        LocalDateTime moderationCutoff = now.minusDays(Math.max(30, moderationRetentionDays));

        int commentReportDeleted = commentReportDao.deleteResolvedByCreatedAtBefore(
                moderationCutoff,
                RESOLVED_COMMENT_REPORT_STATUSES
        );
        int plateRequestDeleted = plateRemovalRequestDao.deleteResolvedByCreatedAtBefore(
                moderationCutoff,
                RESOLVED_PLATE_REQUEST_STATUSES
        );

        if (commentReportDeleted + plateRequestDeleted > 0) {
            log.info(
                    "Compliance retention cleanup done. commentReports={}, plateRemovalRequests={}",
                    commentReportDeleted,
                    plateRequestDeleted
            );
        }
    }
}
