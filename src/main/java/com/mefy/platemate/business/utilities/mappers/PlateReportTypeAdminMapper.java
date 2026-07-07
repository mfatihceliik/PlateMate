package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.dto.PlateReportTypeAdminDto;
import org.springframework.stereotype.Component;

@Component
public class PlateReportTypeAdminMapper implements IMapper<PlateReportType, PlateReportTypeAdminDto> {

    @Override
    public PlateReportTypeAdminDto entityToDto(PlateReportType entity) {
        if (entity == null) {
            return null;
        }
        return new PlateReportTypeAdminDto(
                entity.getId(),
                entity.getCode(),
                entity.getLabel(),
                entity.getDescription(),
                entity.getIconKey(),
                entity.getSeverityId(),
                entity.getSeverityCode(),
                entity.getColorHex(),
                entity.getWeight(),
                entity.getSortOrder(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public PlateReportType dtoToEntity(PlateReportTypeAdminDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
