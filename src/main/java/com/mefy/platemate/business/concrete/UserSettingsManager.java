package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.SocialMediaLinkMapper;
import com.mefy.platemate.core.utilities.mappers.UserSettingsMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSubscriptionDao;
import com.mefy.platemate.entities.concrete.UserSubscription;
import com.mefy.platemate.entities.concrete.UserSubscriptionStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.dto.SocialMediaLinkDto;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import com.mefy.platemate.entities.dto.UserSettingsOverviewDto;
import com.mefy.platemate.entities.dto.request.UpdateSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSettingsManager implements IUserSettingsService {

    private final IUserSettingsDao userSettingsDao;
    private final IUserDao userDao;
    private final IUserProfileDao userProfileDao;
    private final IUserSubscriptionDao userSubscriptionDao;
    private final UserSettingsMapper userSettingsMapper;
    private final SocialMediaLinkMapper socialMediaLinkMapper;
    private final IMessageService messageService;

    @Override
    public DataResult<UserSettingsDto> getByUserId(Long userId) {
        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));
        return new SuccessDataResult<>(userSettingsMapper.entityToDto(settings), messageService.getMessage(Messages.SETTINGS_FOUND));
    }

    @Override
    public DataResult<UserSettingsOverviewDto> getOverviewByUserId(Long userId) {
        User user = userDao.findById(userId).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));
        var profile = userProfileDao.findByIdWithSocialMediaLinks(userId).orElse(null);
        List<SocialMediaLinkDto> socialLinks = profile == null || profile.getSocialMediaLinks() == null
                ? List.of()
                : profile.getSocialMediaLinks().stream().map(socialMediaLinkMapper::entityToDto).toList();

        UserSettingsOverviewDto overviewDto = new UserSettingsOverviewDto(
                user.getEmail(),
                user.isPremiumActive(),
                resolvePremiumUntil(user.getId()),
                userSettingsMapper.entityToDto(settings),
                socialLinks
        );
        return new SuccessDataResult<>(overviewDto, messageService.getMessage(Messages.SETTINGS_FOUND));
    }

    @Override
    public Result updateSettings(Long userId, UpdateSettingsRequest request) {
        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));

        if (request.getMessagingEnabled() != null) {
            settings.setMessagingEnabled(request.getMessagingEnabled());
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
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setMessagingEnabled(true);
        settings.setMessageNotificationsEnabled(true);
        settings.setFriendNotificationsEnabled(true);
        return userSettingsDao.save(settings);
    }

    private LocalDateTime resolvePremiumUntil(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> subscriptions = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(userId);
        return subscriptions.stream()
                .filter(s -> s.getStatus() != UserSubscriptionStatus.CANCELED)
                .map(UserSubscription::getExpiresAt)
                .filter(expiresAt -> expiresAt != null && expiresAt.isAfter(now))
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}
