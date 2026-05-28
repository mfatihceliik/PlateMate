package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.business.utilities.plate.concrete.PlateReportTypePolicyService;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlateReportTypeMapper implements ModelMapperService<PlateReportType, PlateReportTypeDto> {

    private final PlateReportTypePolicyService plateReportTypePolicyService;

    @Override
    public PlateReportTypeDto entityToDto(PlateReportType entity) {
        if (entity == null) return null;

        PlateReportTypeDto dto = new PlateReportTypeDto();
        dto.setCode(entity.getCode());
        dto.setLabel(plateReportTypePolicyService.neutralLabel(entity.getCode(), entity.getLabel()));
        dto.setDescription(plateReportTypePolicyService.neutralDescription(entity.getCode(), entity.getDescription()));
        dto.setIconKey(entity.getIconKey());
        dto.setSeverityId(entity.getSeverityId());
        dto.setSeverityCode(entity.getSeverityCode());
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
