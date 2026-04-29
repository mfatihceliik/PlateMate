package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.UserSettingsMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import com.mefy.platemate.entities.dto.request.UpdateSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingsManager implements IUserSettingsService {

    private final IUserSettingsDao userSettingsDao;
    private final IUserDao userDao;
    private final UserSettingsMapper userSettingsMapper;
    private final IMessageService messageService;

    @Override
    public DataResult<UserSettingsDto> getByUserId(Long userId) {
        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));
        return new SuccessDataResult<>(userSettingsMapper.entityToDto(settings), messageService.getMessage(Messages.SETTINGS_FOUND));
    }

    @Override
    public Result updateSettings(Long userId, UpdateSettingsRequest request) {
        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));

        if (request.getMessagingEnabled() != null) {
            settings.setMessagingEnabled(request.getMessagingEnabled());
        }
        if (request.getLocationSharingEnabled() != null) {
            settings.setLocationSharingEnabled(request.getLocationSharingEnabled());
        }
        if (request.getMessageNotificationsEnabled() != null) {
            settings.setMessageNotificationsEnabled(request.getMessageNotificationsEnabled());
        }
        if (request.getFriendNotificationsEnabled() != null) {
            settings.setFriendNotificationsEnabled(request.getFriendNotificationsEnabled());
        }

        userSettingsDao.save(settings);
        return new SuccessResult(messageService.getMessage(Messages.SETTINGS_UPDATED));
    }

    private UserSettings createDefaultSettings(Long userId) {
        User user = userDao.findById(userId).orElseThrow();
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setMessagingEnabled(true);
        settings.setLocationSharingEnabled(true);
        settings.setMessageNotificationsEnabled(true);
        settings.setFriendNotificationsEnabled(true);
        return userSettingsDao.save(settings);
    }
}
