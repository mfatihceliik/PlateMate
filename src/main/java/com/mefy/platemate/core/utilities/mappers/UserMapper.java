package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements ModelMapperService<User, UserDto> {
    @Override
    public UserDto entityToDto(User entity) {
        if (entity == null) return null;
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setPremiumUntil(entity.getPremiumUntil());
        dto.setPremiumActive(entity.isPremiumActive());
        if (entity.getRole() != null && entity.getRole().getCode() != null) {
            dto.setRoleCode(entity.getRole().getCode().name());
        }
        return dto;
    }

    @Override
    public User dtoToEntity(UserDto dto) {
        return null;
    }
}
