package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import org.springframework.stereotype.Component;

@Component
public class PlateReportTypeMapper implements ModelMapperService<PlateReportType, PlateReportTypeDto> {

    @Override
    public PlateReportTypeDto entityToDto(PlateReportType entity) {
        if (entity == null) return null;

        PlateReportTypeDto dto = new PlateReportTypeDto();
        dto.setCode(entity.getCode());
        dto.setLabel(entity.getLabel());
        dto.setDescription(entity.getDescription());
        dto.setIconKey(entity.getIconKey());
        dto.setSeverity(entity.getSeverity());
        dto.setColorHex(entity.getColorHex());
        dto.setWeight(entity.getWeight());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }

    @Override
    public PlateReportType dtoToEntity(PlateReportTypeDto dto) {
        return null;
    }
}
