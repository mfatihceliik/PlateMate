package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.CommentReportReason;
import com.mefy.platemate.entities.dto.CommentReportReasonAdminDto;
import org.springframework.stereotype.Component;

@Component
public class CommentReportReasonAdminMapper implements IMapper<CommentReportReason, CommentReportReasonAdminDto> {

    @Override
    public CommentReportReasonAdminDto entityToDto(CommentReportReason entity) {
        if (entity == null) {
            return null;
        }
        return new CommentReportReasonAdminDto(
                entity.getId(),
                entity.getCode(),
                entity.getLabel(),
                entity.isRequiresDescription(),
                entity.getSortOrder(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public CommentReportReason dtoToEntity(CommentReportReasonAdminDto dto) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
