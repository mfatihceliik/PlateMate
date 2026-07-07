package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.entities.concrete.SocialMediaLink;
import com.mefy.platemate.entities.dto.SocialMediaLinkDto;
import org.springframework.stereotype.Component;

@Component
public class SocialMediaLinkMapper implements IMapper<SocialMediaLink, SocialMediaLinkDto> {
    @Override
    public SocialMediaLinkDto entityToDto(SocialMediaLink entity) {
        if (entity == null) return null;
        return new SocialMediaLinkDto(
                entity.getId(),
                entity.getPlatformId(),
                entity.getPlatformCode(),
                entity.getUrl()
        );
    }

    @Override
    public SocialMediaLink dtoToEntity(SocialMediaLinkDto dto) {
        return null;
    }
}


