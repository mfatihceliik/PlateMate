package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.dto.PlateDetailDto;
import org.springframework.stereotype.Component;

@Component
public class PlateDetailMapper implements IMapper<Plate, PlateDetailDto> {

    @Override
    public PlateDetailDto entityToDto(Plate entity) {
        if (entity == null) {
            return null;
        }
        PlateDetailDto dto = new PlateDetailDto();
        dto.setId(entity.getId());
        dto.setPlateCode(entity.getPlateCode());
        if (entity.getCity() != null) {
            dto.setCityName(entity.getCity().getName());
        }
        dto.setRatingAverage(entity.getRatingAverage() == null ? 0.0 : entity.getRatingAverage());
        dto.setReviewCount(entity.getReviewCount() == null ? 0 : entity.getReviewCount());
        dto.setTotalRatingSum(entity.getTotalRatingSum() == null ? 0L : entity.getTotalRatingSum());
        return dto;
    }

    @Override
    public Plate dtoToEntity(PlateDetailDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
