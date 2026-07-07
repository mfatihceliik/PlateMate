package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PremiumFeature;
import com.mefy.platemate.entities.dto.PremiumFeatureDto;
import com.mefy.platemate.entities.concrete.PremiumFeatureTranslation;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PremiumFeatureMapper implements IMapper<PremiumFeature, PremiumFeatureDto> {

    @Override
    public PremiumFeatureDto entityToDto(PremiumFeature entity) {
        if (entity == null) {
            return null;
        }
        String lang = LocaleContextHolder.getLocale().getLanguage();
        String title = "";
        String subtitle = "";

        if (entity.getTranslations() != null) {
            PremiumFeatureTranslation translation = entity.getTranslations().stream()
                    .filter(t -> t.getLocale().equalsIgnoreCase(lang))
                    .findFirst()
                    .orElseGet(() -> entity.getTranslations().stream()
                            .filter(t -> t.getLocale().equalsIgnoreCase("en"))
                            .findFirst()
                            .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null)));

            if (translation != null) {
                title = translation.getTitle();
                subtitle = translation.getSubtitle();
            }
        }

        return new PremiumFeatureDto(
                entity.getId(),
                entity.getIconKey(),
                title,
                subtitle,
                entity.getSortOrder()
        );
    }

    @Override
    public PremiumFeature dtoToEntity(PremiumFeatureDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
