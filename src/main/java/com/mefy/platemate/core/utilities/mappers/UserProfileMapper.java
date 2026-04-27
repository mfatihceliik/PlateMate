package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserProfileMapper implements ModelMapperService<UserProfile, UserProfileDto> {

    private final SocialMediaLinkMapper socialMediaLinkMapper;

    @Override
    public UserProfileDto entityToDto(UserProfile entity) {
        if (entity == null) return null;
        UserProfileDto dto = new UserProfileDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBio(entity.getBio());
        dto.setDriverRating(entity.getDriverRating());
        dto.setReviewCount(entity.getReviewCount());

        if (entity.getSocialMediaLinks() != null) {
            dto.setSocialMediaLinks(entity.getSocialMediaLinks().stream()
                    .map(socialMediaLinkMapper::entityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Override
    public UserProfile dtoToEntity(UserProfileDto dto) {
        return null;
    }
}
