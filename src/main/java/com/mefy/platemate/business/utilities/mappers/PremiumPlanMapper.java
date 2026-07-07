package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PremiumPlan;
import com.mefy.platemate.entities.concrete.PremiumPlanTranslation;
import com.mefy.platemate.entities.dto.PremiumPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumPlanMapper implements IMapper<PremiumPlan, PremiumPlanDto> {

    private final LocalizedEnumService localizedEnumService;

    @Override
    public PremiumPlanDto entityToDto(PremiumPlan entity) {
        if (entity == null) {
            return null;
        }

        String lang = LocaleContextHolder.getLocale().getLanguage();
        String title = "";
        String description = "";

        if (entity.getTranslations() != null) {
            PremiumPlanTranslation translation = entity.getTranslations().stream()
                    .filter(t -> t.getLocale().equalsIgnoreCase(lang))
                    .findFirst()
                    .orElseGet(() -> entity.getTranslations().stream()
                            .filter(t -> t.getLocale().equalsIgnoreCase("en"))
                            .findFirst()
                            .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null)));

            if (translation != null) {
                title = translation.getTitle();
                description = translation.getDescription();
            }
        }

        return new PremiumPlanDto(
                entity.getId(),
                entity.getPeriod(),
                localizedEnumService != null && entity.getPeriod() != null ? localizedEnumService.label("premium_period", entity.getPeriod()) : null,
                title,
                description,
                entity.getAmount(),
                entity.getCurrency(),
                entity.getDiscountPercent(),
                entity.getSortOrder()
        );
    }

    @Override
    public PremiumPlan dtoToEntity(PremiumPlanDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
