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
        if (entity.getProfile() != null) {
            dto.setFirstName(entity.getProfile().getFirstName());
            dto.setLastName(entity.getProfile().getLastName());
        }
        return dto;
    }

    @Override
    public User dtoToEntity(UserDto dto) { return null; }
}
