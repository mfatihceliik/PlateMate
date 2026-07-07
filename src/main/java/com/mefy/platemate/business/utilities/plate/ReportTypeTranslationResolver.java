package com.mefy.platemate.business.utilities.plate;

import com.mefy.platemate.business.utilities.plate.concrete.PlateReportTypePolicyService;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeTranslationDao;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.concrete.PlateReportTypeTranslation;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportTypeTranslationResolver {

    private final IPlateReportTypeTranslationDao translationDao;
    private final PlateReportTypePolicyService plateReportTypePolicyService;

    public Map<Long, PlateReportTypeTranslation> loadTranslations() {
        String locale = LocaleContextHolder.getLocale().getLanguage();
        return translationDao.findByLocale(locale).stream()
                .collect(Collectors.toMap(PlateReportTypeTranslation::getReportTypeId, t -> t));
    }

    public String resolveLabel(PlateReportType type, Map<Long, PlateReportTypeTranslation> translationMap) {
        if (type == null) return null;
        PlateReportTypeTranslation translation = translationMap != null ? translationMap.get(type.getId()) : null;
        return translation != null
                ? translation.getLabel()
                : plateReportTypePolicyService.neutralLabel(type.getCode(), type.getLabel());
    }

    public String resolveDescription(PlateReportType type, Map<Long, PlateReportTypeTranslation> translationMap) {
        if (type == null) return null;
        PlateReportTypeTranslation translation = translationMap != null ? translationMap.get(type.getId()) : null;
        return translation != null && translation.getDescription() != null
                ? translation.getDescription()
                : plateReportTypePolicyService.neutralDescription(type.getCode(), type.getDescription());
    }
}
