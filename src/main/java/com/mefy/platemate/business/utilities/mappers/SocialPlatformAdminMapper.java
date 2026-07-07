package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.SocialPlatformLookup;
import com.mefy.platemate.entities.dto.SocialPlatformAdminDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SocialPlatformAdminMapper implements IMapper<SocialPlatformLookup, SocialPlatformAdminDto> {

    @Override
    public SocialPlatformAdminDto entityToDto(SocialPlatformLookup entity) {
        if (entity == null) {
            return null;
        }
        Map<String, String> labels = entity.getTranslations().stream()
                .collect(Collectors.toMap(t -> t.getLocale(), t -> t.getLabel()));

        return new SocialPlatformAdminDto(
                entity.getId(),
                entity.getCode(),
                labels,
                entity.getIconUrl(),
                entity.getBaseUrl(),
                entity.getBackgroundColorHex(),
                entity.getIconTintColorHex(),
                entity.getSortOrder(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public SocialPlatformLookup dtoToEntity(SocialPlatformAdminDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
