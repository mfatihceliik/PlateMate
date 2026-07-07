package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.dto.PlateListItemDto;
import org.springframework.stereotype.Component;

@Component
public class UserPlateListMapper implements IMapper<Plate, PlateListItemDto> {

    @Override
    public PlateListItemDto entityToDto(Plate entity) {
        if (entity == null) {
            return null;
        }
        PlateListItemDto dto = new PlateListItemDto();
        dto.setPlateCode(entity.getPlateCode());
        if (entity.getCity() != null) {
            dto.setCityName(entity.getCity().getName());
        }
        dto.setRatingAverage(entity.getRatingAverage());
        dto.setReviewCount(entity.getReviewCount());
        return dto;
    }

    @Override
    public Plate dtoToEntity(PlateListItemDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
