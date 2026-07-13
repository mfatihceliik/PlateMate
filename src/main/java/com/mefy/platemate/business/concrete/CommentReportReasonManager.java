package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ICommentReportReasonService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.business.utilities.mappers.CommentReportReasonAdminMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ICommentReportReasonDao;
import com.mefy.platemate.entities.concrete.CommentReportReason;
import com.mefy.platemate.entities.dto.CommentReportReasonAdminDto;
import com.mefy.platemate.entities.dto.CommentReportReasonDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdateCommentReportReasonRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CommentReportReasonManager implements ICommentReportReasonService {

    private static final String ENUM_CATEGORY = "comment_report_reason";

    private final ICommentReportReasonDao commentReportReasonDao;
    private final CommentReportReasonAdminMapper commentReportReasonAdminMapper;
    private final LocalizedEnumService localizedEnumService;
    private final IMessageService messageService;

    @Override
    public DataResult<List<CommentReportReasonDto>> getActiveReasons() {
        List<CommentReportReasonDto> reasons = commentReportReasonDao.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(reason -> new CommentReportReasonDto(
                        reason.getId(),
                        reason.getCode(),
                        localizedEnumService.label(ENUM_CATEGORY, reason.getCode(), reason.getLabel()),
                        reason.isRequiresDescription()
                ))
                .toList();
        return new SuccessDataResult<>(reasons, messageService.getMessage(Messages.COMMENT_REPORT_REASONS_LISTED));
    }

    @Override
    public DataResult<List<CommentReportReasonAdminDto>> getAllReasons() {
        List<CommentReportReasonAdminDto> reasons = commentReportReasonDao.findAllByOrderBySortOrderAsc()
                .stream()
                .map(commentReportReasonAdminMapper::entityToDto)
                .toList();
        return new SuccessDataResult<>(reasons, messageService.getMessage(Messages.COMMENT_REPORT_REASONS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<CommentReportReasonAdminDto> addReason(AddCommentReportReasonRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (commentReportReasonDao.existsByCode(normalizedCode)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.COMMENT_REPORT_REASON_ALREADY_EXISTS));
        }

        LocalDateTime now = LocalDateTime.now();
        CommentReportReason reason = new CommentReportReason();
        reason.setCode(normalizedCode);
        reason.setLabel(request.getLabel().trim());
        reason.setRequiresDescription(Boolean.TRUE.equals(request.getRequiresDescription()));
        reason.setSortOrder(request.getSortOrder());
        reason.setActive(true);
        reason.setCreatedAt(now);
        reason.setUpdatedAt(now);
        commentReportReasonDao.save(reason);

        return new SuccessDataResult<>(
                commentReportReasonAdminMapper.entityToDto(reason),
                messageService.getMessage(Messages.COMMENT_REPORT_REASON_ADDED)
        );
    }

    @Override
    @Transactional
    public DataResult<CommentReportReasonAdminDto> updateReason(Long id, UpdateCommentReportReasonRequest request) {
        CommentReportReason reason = commentReportReasonDao.findById(id).orElse(null);
        if (reason == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.COMMENT_REPORT_REASON_NOT_FOUND));
        }

        String normalizedCode = normalizeCode(request.getCode());
        CommentReportReason existingByCode = commentReportReasonDao.findByCode(normalizedCode).orElse(null);
        if (existingByCode != null && !existingByCode.getId().equals(id)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.COMMENT_REPORT_REASON_ALREADY_EXISTS));
        }

        reason.setCode(normalizedCode);
        reason.setLabel(request.getLabel().trim());
        reason.setRequiresDescription(Boolean.TRUE.equals(request.getRequiresDescription()));
        reason.setSortOrder(request.getSortOrder());
        reason.setUpdatedAt(LocalDateTime.now());
        commentReportReasonDao.save(reason);

        return new SuccessDataResult<>(
                commentReportReasonAdminMapper.entityToDto(reason),
                messageService.getMessage(Messages.COMMENT_REPORT_REASON_UPDATED)
        );
    }

    @Override
    @Transactional
    public Result setReasonActive(Long id, boolean active) {
        CommentReportReason reason = commentReportReasonDao.findById(id).orElse(null);
        if (reason == null) {
            return new ErrorResult(messageService.getMessage(Messages.COMMENT_REPORT_REASON_NOT_FOUND));
        }

        reason.setActive(active);
        reason.setUpdatedAt(LocalDateTime.now());
        commentReportReasonDao.save(reason);

        return new SuccessResult(messageService.getMessage(Messages.COMMENT_REPORT_REASON_STATUS_UPDATED));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
