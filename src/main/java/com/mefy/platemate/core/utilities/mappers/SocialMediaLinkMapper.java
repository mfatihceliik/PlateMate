package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.SocialMediaLink;
import com.mefy.platemate.entities.dto.SocialMediaLinkDto;
import org.springframework.stereotype.Component;

@Component
public class SocialMediaLinkMapper implements ModelMapperService<SocialMediaLink, SocialMediaLinkDto> {
    @Override
    public SocialMediaLinkDto entityToDto(SocialMediaLink entity) {
        if (entity == null) return null;
        return new SocialMediaLinkDto(entity.getPlatform(), entity.getUrl());
    }

    @Override
    public SocialMediaLink dtoToEntity(SocialMediaLinkDto dto) {
        return null;
    }
}
