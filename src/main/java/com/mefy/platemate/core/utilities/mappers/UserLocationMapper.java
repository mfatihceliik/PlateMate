package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.UserLocation;
import com.mefy.platemate.entities.dto.UserLocationDto;
import org.springframework.stereotype.Component;

@Component
public class UserLocationMapper implements ModelMapperService<UserLocation, UserLocationDto> {

    @Override
    public UserLocationDto entityToDto(UserLocation entity) {
        if (entity == null) return null;
        UserLocationDto dto = new UserLocationDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUsername(entity.getUser().getUsername());
        }
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setLastUpdatedAt(entity.getLastUpdatedAt());
        return dto;
    }

    @Override
    public UserLocation dtoToEntity(UserLocationDto dto) {
        return null;
    }
}
