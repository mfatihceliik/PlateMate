package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.SocialPlatformLookup;
import com.mefy.platemate.entities.concrete.SocialPlatformTranslation;
import com.mefy.platemate.entities.dto.SocialPlatformDto;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SocialPlatformMapper implements IMapper<SocialPlatformLookup, SocialPlatformDto> {

    @Override
    public SocialPlatformDto entityToDto(SocialPlatformLookup entity) {
        if (entity == null) {
            return null;
        }

        String lang = LocaleContextHolder.getLocale().getLanguage();
        String label = "";

        if (entity.getTranslations() != null) {
            SocialPlatformTranslation translation = entity.getTranslations().stream()
                    .filter(t -> t.getLocale().equalsIgnoreCase(lang))
                    .findFirst()
                    .orElseGet(() -> entity.getTranslations().stream()
                            .filter(t -> t.getLocale().equalsIgnoreCase("en"))
                            .findFirst()
                            .orElseGet(() -> entity.getTranslations().stream().findFirst().orElse(null)));

            if (translation != null) {
                label = translation.getLabel();
            }
        }

        return new SocialPlatformDto(
                entity.getId(),
                entity.getCode(),
                label,
                entity.getIconUrl(),
                entity.getBaseUrl(),
                entity.getBackgroundColorHex(),
                entity.getIconTintColorHex(),
                entity.getSortOrder()
        );
    }

    @Override
    public SocialPlatformLookup dtoToEntity(SocialPlatformDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
