package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import org.springframework.stereotype.Component;

@Component
public class UserSettingsMapper implements IMapper<UserSettings, UserSettingsDto> {
    @Override
    public UserSettingsDto entityToDto(UserSettings entity) {
        if (entity == null) return null;
        UserSettingsDto dto = new UserSettingsDto();
        dto.setMessagingEnabled(entity.isMessagingEnabled());
        dto.setOnlineVisibilityEnabled(entity.isOnlineVisibilityEnabled());
        dto.setMessageNotificationsEnabled(entity.isMessageNotificationsEnabled());
        dto.setFriendNotificationsEnabled(entity.isFriendNotificationsEnabled());
        dto.setPlateReviewNotificationsEnabled(entity.isPlateReviewNotificationsEnabled());
        dto.setNewFollowerNotificationsEnabled(entity.isNewFollowerNotificationsEnabled());
        dto.setReviewReplyNotificationsEnabled(entity.isReviewReplyNotificationsEnabled());
        return dto;
    }

    @Override
    public UserSettings dtoToEntity(UserSettingsDto dto) {
        return null; // Not needed for now
    }
}


