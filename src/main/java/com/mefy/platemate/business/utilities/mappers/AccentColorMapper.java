package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.AccentColor;
import com.mefy.platemate.entities.dto.AccentColorDto;
import org.springframework.stereotype.Component;

@Component
public class AccentColorMapper implements IMapper<AccentColor, AccentColorDto> {

    @Override
    public AccentColorDto entityToDto(AccentColor entity) {
        if (entity == null) {
            return null;
        }
        return new AccentColorDto(entity.getId(), entity.getHex(), entity.getSortOrder());
    }

    @Override
    public AccentColor dtoToEntity(AccentColorDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
