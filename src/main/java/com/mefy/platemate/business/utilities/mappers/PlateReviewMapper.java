package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlateReviewMapper implements IMapper<PlateReview, PlateReviewDto> {

    // Field-injected cross-cutting i18n helper: optional so no-arg unit constructions stay valid.
    @Autowired(required = false)
    private LocalizedEnumService localizedEnumService;

    @Override
    public PlateReviewDto entityToDto(PlateReview entity) {
        if (entity == null) return null;

        PlateReviewDto dto = new PlateReviewDto();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setReviewStatusId(entity.getStatusId());
        dto.setReviewStatusCode(entity.getStatusCode());
        if (localizedEnumService != null) {
            dto.setReviewStatusLabel(localizedEnumService.label("review_status", entity.getStatusCode()));
        }
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


