package com.mefy.platemate.business.concrete;

import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateRemovalRequestDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.AdminMenuItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMenuManagerTest {

    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private ICommentReportDao commentReportDao;
    @Mock
    private IPlateRemovalRequestDao plateRemovalRequestDao;
    @Mock
    private IPlateDao plateDao;
    @Mock
    private IMessageService messageService;

    private AdminMenuManager manager;

    @BeforeEach
    void setUp() {
        manager = new AdminMenuManager(
                plateReviewDao,
                commentReportDao,
                plateRemovalRequestDao,
                plateDao,
                messageService
        );
    }

    @Test
    void getMenuReturnsAllEntriesWithLocalizedTitlesAndModerationBadges() {
        when(messageService.getMessage(anyString())).thenAnswer(invocation -> "title:" + invocation.getArgument(0));
        when(plateReviewDao.countByStatusId(PlateReviewStatus.PENDING_REVIEW.getId())).thenReturn(12L);
        when(commentReportDao.countByStatusId(CommentReportStatus.OPEN.getId())).thenReturn(3L);
        when(plateRemovalRequestDao.countByStatusId(PlateRemovalRequestStatus.OPEN.getId())).thenReturn(1L);
        when(plateDao.countByStatusIdIn(argThat(ids ->
                ids.containsAll(List.of(
                        PlateStatus.HIDDEN_BY_REQUEST.getId(),
                        PlateStatus.BLOCKED.getId(),
                        PlateStatus.DELETED.getId()
                ))
        ))).thenReturn(5L);

        DataResult<List<AdminMenuItemDto>> result = manager.getMenu();

        assertTrue(result.isSuccess());
        assertEquals(11, result.getData().size());

        // Sort order preserved and unique.
        for (int i = 0; i < result.getData().size(); i++) {
            assertEquals(i + 1, result.getData().get(i).getSortOrder());
        }

        Map<String, AdminMenuItemDto> byCode = result.getData().stream()
                .collect(Collectors.toMap(AdminMenuItemDto::getCode, Function.identity()));

        assertEquals(12L, byCode.get("PENDING_COMMENTS").getBadgeCount());
        assertEquals(3L, byCode.get("COMMENT_REPORTS").getBadgeCount());
        assertEquals(1L, byCode.get("PLATE_REMOVAL_REQUESTS").getBadgeCount());
        assertEquals(5L, byCode.get("HIDDEN_PLATES").getBadgeCount());
        assertNull(byCode.get("PLATE_REPORT_TYPES").getBadgeCount());
        assertNull(byCode.get("APP_SETTINGS").getBadgeCount());

        assertEquals("title:admin.menu.pending.comments", byCode.get("PENDING_COMMENTS").getTitle());
        assertEquals("comment", byCode.get("PENDING_COMMENTS").getIconKey());
    }
}
