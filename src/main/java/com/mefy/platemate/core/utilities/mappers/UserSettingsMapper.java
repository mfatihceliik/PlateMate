package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import org.springframework.stereotype.Component;

@Component
public class UserSettingsMapper implements ModelMapperService<UserSettings, UserSettingsDto> {
    @Override
    public UserSettingsDto entityToDto(UserSettings entity) {
        if (entity == null) return null;
        UserSettingsDto dto = new UserSettingsDto();
        dto.setMessagingEnabled(entity.isMessagingEnabled());
        dto.setLocationSharingEnabled(entity.isLocationSharingEnabled());
        dto.setNotificationsEnabled(entity.isNotificationsEnabled());
        return dto;
    }

    @Override
    public UserSettings dtoToEntity(UserSettingsDto dto) {
        return null; // Şimdilik ihtiyaç yok
    }
}
