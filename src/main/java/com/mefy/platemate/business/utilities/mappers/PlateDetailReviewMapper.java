package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.dto.PlateDetailReviewDto;
import org.springframework.stereotype.Component;

@Component
public class PlateDetailReviewMapper implements IMapper<PlateReview, PlateDetailReviewDto> {

    @Override
    public PlateDetailReviewDto entityToDto(PlateReview entity) {
        if (entity == null) {
            return null;
        }
        PlateDetailReviewDto dto = new PlateDetailReviewDto();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUsername(entity.getUser().getUsername());
            if (entity.getUser().getProfile() != null) {
                dto.setDisplayName(entity.getUser().getProfile().getDisplayName());
                dto.setProfilePhotoUrl(entity.getUser().getProfile().getProfilePhotoUrl());
            }
        }
        return dto;
    }

    @Override
    public PlateReview dtoToEntity(PlateDetailReviewDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
