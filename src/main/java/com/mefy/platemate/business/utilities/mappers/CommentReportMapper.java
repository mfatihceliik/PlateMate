package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.CommentReport;
import com.mefy.platemate.entities.dto.CommentReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentReportMapper implements IMapper<CommentReport, CommentReportDto> {

    private final LocalizedEnumService localizedEnumService;

    @Override
    public CommentReportDto entityToDto(CommentReport entity) {
        if (entity == null) {
            return null;
        }
        String plateCode = null;
        if (entity.getComment() != null && entity.getComment().getPlate() != null) {
            plateCode = entity.getComment().getPlate().getPlateCode();
        }
        CommentReportDto dto = new CommentReportDto();
        dto.setId(entity.getId());
        dto.setCommentId(entity.getComment() != null ? entity.getComment().getId() : null);
        dto.setReporterUserId(entity.getReporterUserId());
        dto.setPlateCode(plateCode);
        dto.setReasonId(entity.getReasonId());
        dto.setReasonCode(entity.getReasonCode());
        dto.setDescription(entity.getDescription());
        dto.setStatusId(entity.getStatusId());
        dto.setStatusCode(entity.getStatusCode());
        dto.setAdminNote(entity.getAdminNote());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setReviewedBy(entity.getReviewedBy());
        if (localizedEnumService != null) {
            String reasonFallback = entity.getReason() == null ? entity.getReasonCode() : entity.getReason().getLabel();
            dto.setReasonLabel(localizedEnumService.label("comment_report_reason", entity.getReasonCode(), reasonFallback));
            dto.setStatusLabel(localizedEnumService.label("comment_report_status", entity.getStatusCode()));
        }
        return dto;
    }

    @Override
    public CommentReport dtoToEntity(CommentReportDto dto) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
