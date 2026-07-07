package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.business.utilities.plate.ReportTypeTranslationResolver;
import com.mefy.platemate.business.utilities.plate.concrete.PlateReportTypePolicyService;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.concrete.PlateReportTypeTranslation;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PlateReportTypeMapper implements IMapper<PlateReportType, PlateReportTypeDto> {

    private final PlateReportTypePolicyService plateReportTypePolicyService;
    private final LocalizedEnumService localizedEnumService;
    private final ReportTypeTranslationResolver translationResolver;

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
        if (localizedEnumService != null && entity.getSeverityCode() != null) {
            dto.setSeverityLabel(localizedEnumService.label("report_severity", entity.getSeverityCode()));
        }
        dto.setColorHex(entity.getColorHex());
        dto.setWeight(entity.getWeight());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }

    public PlateReportTypeDto entityToDto(PlateReportType entity, Map<Long, PlateReportTypeTranslation> translationMap) {
        PlateReportTypeDto dto = entityToDto(entity);
        if (dto != null) {
            dto.setLabel(translationResolver.resolveLabel(entity, translationMap));
            dto.setDescription(translationResolver.resolveDescription(entity, translationMap));
        }
        return dto;
    }

    @Override
    public PlateReportType dtoToEntity(PlateReportTypeDto dto) {
        return null;
    }
}


