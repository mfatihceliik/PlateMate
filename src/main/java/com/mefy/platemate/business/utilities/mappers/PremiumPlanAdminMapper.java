package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PremiumPlan;
import com.mefy.platemate.entities.dto.PremiumPlanAdminDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PremiumPlanAdminMapper implements IMapper<PremiumPlan, PremiumPlanAdminDto> {

    @Override
    public PremiumPlanAdminDto entityToDto(PremiumPlan entity) {
        if (entity == null) {
            return null;
        }
        Map<String, String> titles = entity.getTranslations().stream()
                .collect(Collectors.toMap(t -> t.getLocale(), t -> t.getTitle()));
        
        Map<String, String> descriptions = entity.getTranslations().stream()
                .filter(t -> t.getDescription() != null)
                .collect(Collectors.toMap(t -> t.getLocale(), t -> t.getDescription()));

        return new PremiumPlanAdminDto(
                entity.getId(),
                entity.getPeriod(),
                titles,
                descriptions,
                entity.getAmount(),
                entity.getCurrency(),
                entity.getDiscountPercent(),
                entity.getSortOrder(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public PremiumPlan dtoToEntity(PremiumPlanAdminDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
