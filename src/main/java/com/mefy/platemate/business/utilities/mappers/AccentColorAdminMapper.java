package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.AccentColor;
import com.mefy.platemate.entities.dto.AccentColorAdminDto;
import org.springframework.stereotype.Component;

@Component
public class AccentColorAdminMapper implements IMapper<AccentColor, AccentColorAdminDto> {

    @Override
    public AccentColorAdminDto entityToDto(AccentColor entity) {
        if (entity == null) {
            return null;
        }
        return new AccentColorAdminDto(
                entity.getId(),
                entity.getHex(),
                entity.getSortOrder(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public AccentColor dtoToEntity(AccentColorAdminDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
