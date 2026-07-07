package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PremiumFeature;
import com.mefy.platemate.entities.dto.PremiumFeatureAdminDto;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PremiumFeatureAdminMapper implements IMapper<PremiumFeature, PremiumFeatureAdminDto> {

    @Override
    public PremiumFeatureAdminDto entityToDto(PremiumFeature entity) {
        if (entity == null) {
            return null;
        }
        Map<String, String> titles = entity.getTranslations().stream()
                .collect(Collectors.toMap(t -> t.getLocale(), t -> t.getTitle()));
        
        Map<String, String> subtitles = entity.getTranslations().stream()
                .filter(t -> t.getSubtitle() != null)
                .collect(Collectors.toMap(t -> t.getLocale(), t -> t.getSubtitle()));

        return new PremiumFeatureAdminDto(
                entity.getId(),
                entity.getIconKey(),
                titles,
                subtitles,
                entity.getSortOrder(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public PremiumFeature dtoToEntity(PremiumFeatureAdminDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
