package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestReason;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonAdminDto;
import org.springframework.stereotype.Component;

@Component
public class PlateRemovalRequestReasonAdminMapper implements IMapper<PlateRemovalRequestReason, PlateRemovalRequestReasonAdminDto> {

    @Override
    public PlateRemovalRequestReasonAdminDto entityToDto(PlateRemovalRequestReason entity) {
        if (entity == null) {
            return null;
        }
        return new PlateRemovalRequestReasonAdminDto(
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
    public PlateRemovalRequestReason dtoToEntity(PlateRemovalRequestReasonAdminDto dto) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
