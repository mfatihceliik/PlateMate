package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.business.utilities.mappers.CommentReportReasonAdminMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportReasonDao;
import com.mefy.platemate.entities.concrete.CommentReportReason;
import com.mefy.platemate.entities.dto.CommentReportReasonAdminDto;
import com.mefy.platemate.entities.dto.CommentReportReasonDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdateCommentReportReasonRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentReportReasonManagerTest {

    @Mock
    private ICommentReportReasonDao commentReportReasonDao;
    @Mock
    private LocalizedEnumService localizedEnumService;
    @Mock
    private IMessageService messageService;

    private CommentReportReasonManager manager;

    @BeforeEach
    void setUp() {
        manager = new CommentReportReasonManager(
                commentReportReasonDao,
                new CommentReportReasonAdminMapper(),
                localizedEnumService,
                messageService
        );
    }

    private CommentReportReason reason(Long id, String code, String label, boolean requiresDescription) {
        CommentReportReason reason = new CommentReportReason();
        reason.setId(id);
        reason.setCode(code);
        reason.setLabel(label);
        reason.setRequiresDescription(requiresDescription);
        reason.setSortOrder(id.intValue());
        reason.setActive(true);
        reason.setCreatedAt(LocalDateTime.now());
        reason.setUpdatedAt(LocalDateTime.now());
        return reason;
    }

    @Test
    void getActiveReasonsReturnsLocalizedLabelsWithEntityFallback() {
        when(commentReportReasonDao.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(
                reason(6L, "SPAM", "Spam", false),
                reason(7L, "OTHER", "Other", true),
                reason(8L, "CUSTOM_REASON", "Admin girdi", false)
        ));
        when(localizedEnumService.label(eq("comment_report_reason"), eq("SPAM"), eq("Spam"))).thenReturn("Spam TR");
        when(localizedEnumService.label(eq("comment_report_reason"), eq("OTHER"), eq("Other"))).thenReturn("Diger");
        when(localizedEnumService.label(eq("comment_report_reason"), eq("CUSTOM_REASON"), eq("Admin girdi")))
                .thenReturn("Admin girdi");
        when(messageService.getMessage("comment.report.reasons.listed")).thenReturn("listed");

        DataResult<List<CommentReportReasonDto>> result = manager.getActiveReasons();

        assertTrue(result.isSuccess());
        assertEquals(3, result.getData().size());
        assertEquals("Spam TR", result.getData().get(0).getLabel());
        assertTrue(result.getData().get(1).isRequiresDescription());
        assertEquals("Admin girdi", result.getData().get(2).getLabel());
        assertFalse(result.getData().get(2).isRequiresDescription());
    }

    @Test
    void addReasonRejectsDuplicateCode() {
        when(commentReportReasonDao.existsByCode("SPAM")).thenReturn(true);
        when(messageService.getMessage("comment.report.reason.already.exists")).thenReturn("exists");

        DataResult<CommentReportReasonAdminDto> result =
                manager.addReason(new AddCommentReportReasonRequest(" spam ", "Spam", false, 6));

        assertFalse(result.isSuccess());
        assertEquals("exists", result.getMessage());
        verify(commentReportReasonDao, never()).save(any());
    }

    @Test
    void addReasonNormalizesCodeAndSaves() {
        when(commentReportReasonDao.existsByCode("FAKE_PLATE")).thenReturn(false);
        when(messageService.getMessage("comment.report.reason.added")).thenReturn("added");

        DataResult<CommentReportReasonAdminDto> result =
                manager.addReason(new AddCommentReportReasonRequest("fake plate", "  Sahte plaka  ", true, 8));

        assertTrue(result.isSuccess());
        ArgumentCaptor<CommentReportReason> captor = ArgumentCaptor.forClass(CommentReportReason.class);
        verify(commentReportReasonDao).save(captor.capture());
        assertEquals("FAKE_PLATE", captor.getValue().getCode());
        assertEquals("Sahte plaka", captor.getValue().getLabel());
        assertTrue(captor.getValue().isRequiresDescription());
        assertTrue(captor.getValue().isActive());
        assertEquals("FAKE_PLATE", result.getData().getCode());
    }

    @Test
    void updateReasonRejectsUnknownIdAndCodeCollision() {
        when(commentReportReasonDao.findById(99L)).thenReturn(Optional.empty());
        when(messageService.getMessage("comment.report.reason.not.found")).thenReturn("notfound");

        DataResult<CommentReportReasonAdminDto> notFound =
                manager.updateReason(99L, new UpdateCommentReportReasonRequest("SPAM", "Spam", false, 6));
        assertFalse(notFound.isSuccess());
        assertEquals("notfound", notFound.getMessage());

        when(commentReportReasonDao.findById(7L)).thenReturn(Optional.of(reason(7L, "OTHER", "Other", true)));
        when(commentReportReasonDao.findByCode("SPAM")).thenReturn(Optional.of(reason(6L, "SPAM", "Spam", false)));
        when(messageService.getMessage("comment.report.reason.already.exists")).thenReturn("exists");

        DataResult<CommentReportReasonAdminDto> collision =
                manager.updateReason(7L, new UpdateCommentReportReasonRequest("SPAM", "Spam", false, 6));
        assertFalse(collision.isSuccess());
        assertEquals("exists", collision.getMessage());
        verify(commentReportReasonDao, never()).save(any());
    }

    @Test
    void setReasonActiveTogglesFlag() {
        CommentReportReason spam = reason(6L, "SPAM", "Spam", false);
        when(commentReportReasonDao.findById(6L)).thenReturn(Optional.of(spam));
        when(messageService.getMessage("comment.report.reason.status.updated")).thenReturn("updated");

        Result result = manager.setReasonActive(6L, false);

        assertTrue(result.isSuccess());
        assertFalse(spam.isActive());
        verify(commentReportReasonDao).save(spam);
    }
}
