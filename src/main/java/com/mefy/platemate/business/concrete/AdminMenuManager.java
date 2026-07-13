package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAdminMenuService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateRemovalRequestDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.AdminMenuItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

/**
 * Serves the admin hub menu. Entries are code-defined (a new screen needs client code
 * anyway); titles are localized per request locale, moderation entries carry live
 * pending-work badge counts. Codes are a stable client contract — do not rename.
 */
@Service
@RequiredArgsConstructor
public class AdminMenuManager implements IAdminMenuService {

    private final IPlateReviewDao plateReviewDao;
    private final ICommentReportDao commentReportDao;
    private final IPlateRemovalRequestDao plateRemovalRequestDao;
    private final IPlateDao plateDao;
    private final IMessageService messageService;

    @Override
    public DataResult<List<AdminMenuItemDto>> getMenu() {
        // Same status set as ModerationAdminManager.getHiddenPlates.
        List<Long> hiddenStatusIds = Stream.of(PlateStatus.HIDDEN_BY_REQUEST, PlateStatus.BLOCKED, PlateStatus.DELETED)
                .map(PlateStatus::getId)
                .toList();

        List<AdminMenuItemDto> items = List.of(
                item("PENDING_COMMENTS", Messages.ADMIN_MENU_PENDING_COMMENTS, "comment", 1,
                        plateReviewDao.countByStatusId(PlateReviewStatus.PENDING_REVIEW.getId())),
                item("COMMENT_REPORTS", Messages.ADMIN_MENU_COMMENT_REPORTS, "flag", 2,
                        commentReportDao.countByStatusId(CommentReportStatus.OPEN.getId())),
                item("PLATE_REMOVAL_REQUESTS", Messages.ADMIN_MENU_PLATE_REMOVAL, "rate_review", 3,
                        plateRemovalRequestDao.countByStatusId(PlateRemovalRequestStatus.OPEN.getId())),
                item("HIDDEN_PLATES", Messages.ADMIN_MENU_HIDDEN_PLATES, "visibility_off", 4,
                        plateDao.countByStatusIdIn(hiddenStatusIds)),
                item("PLATE_REPORT_TYPES", Messages.ADMIN_MENU_REPORT_TYPES, "label", 5, null),
                item("COMMENT_REPORT_REASONS", Messages.ADMIN_MENU_COMMENT_REASONS, "report", 6, null),
                item("SOCIAL_PLATFORMS", Messages.ADMIN_MENU_SOCIAL_PLATFORMS, "share", 7, null),
                item("PREMIUM_PLANS", Messages.ADMIN_MENU_PREMIUM_PLANS, "workspace_premium", 8, null),
                item("PREMIUM_FEATURES", Messages.ADMIN_MENU_PREMIUM_FEATURES, "star", 9, null),
                item("THEME_COLORS", Messages.ADMIN_MENU_THEME_COLORS, "palette", 10, null),
                item("APP_SETTINGS", Messages.ADMIN_MENU_APP_SETTINGS, "tune", 11, null)
        );
        return new SuccessDataResult<>(items, messageService.getMessage(Messages.ADMIN_MENU_LISTED));
    }

    private AdminMenuItemDto item(String code, String titleKey, String iconKey, int sortOrder, Long badgeCount) {
        return new AdminMenuItemDto(code, messageService.getMessage(titleKey), iconKey, sortOrder, badgeCount);
    }
}
