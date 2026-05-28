package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import org.springframework.stereotype.Component;

@Component
public class PlateReviewMapper implements ModelMapperService<PlateReview, PlateReviewDto> {
    @Override
    public PlateReviewDto entityToDto(PlateReview entity) {
        if (entity == null) return null;

        PlateReviewDto dto = new PlateReviewDto();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setReviewStatusId(entity.getStatusId());
        dto.setReviewStatusCode(entity.getStatusCode());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getPlate() != null) {
            dto.setPlateCode(entity.getPlate().getPlateCode());
        }

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUsername(entity.getUser().getUsername());
        }

        return dto;
    }

    @Override
    public PlateReview dtoToEntity(PlateReviewDto dto) {
        return null;
    }
}
