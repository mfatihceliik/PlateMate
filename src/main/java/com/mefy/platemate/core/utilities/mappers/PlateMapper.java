package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.dto.PlateDto;
import org.springframework.stereotype.Component;

@Component
public class PlateMapper implements ModelMapperService<Plate, PlateDto> {
    @Override
    public PlateDto entityToDto(Plate entity) {
        if (entity == null) return null;

        PlateDto dto = new PlateDto();
        dto.setId(entity.getId());
        dto.setPlateCode(entity.getPlateCode());
        dto.setRatingAverage(entity.getRatingAverage() == null ? 0.0 : entity.getRatingAverage());
        dto.setReviewCount(entity.getReviewCount() == null ? 0 : entity.getReviewCount());
        dto.setTotalRatingSum(entity.getTotalRatingSum() == null ? 0L : entity.getTotalRatingSum());
        if (entity.getCity() != null) {
            dto.setCityName(entity.getCity().getName());
        }
        return dto;
    }

    @Override
    public Plate dtoToEntity(PlateDto dto) {
        return null;
    }
}
