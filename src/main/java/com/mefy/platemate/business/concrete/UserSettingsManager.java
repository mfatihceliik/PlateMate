package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ISubscriptionService;
import com.mefy.platemate.business.abstracts.IUserSettingsService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.SocialMediaLinkMapper;
import com.mefy.platemate.business.utilities.mappers.UserSettingsMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.dto.SocialMediaLinkDto;
import com.mefy.platemate.entities.dto.UserSettingsDto;
import com.mefy.platemate.entities.dto.UserSettingsOverviewDto;
import com.mefy.platemate.entities.dto.request.UpdateAppearanceRequest;
import com.mefy.platemate.entities.dto.request.UpdateSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSettingsManager implements IUserSettingsService {

    private final IUserSettingsDao userSettingsDao;
    private final IUserDao userDao;
    private final IUserProfileDao userProfileDao;
    private final ISubscriptionService subscriptionService;
    private final UserSettingsMapper userSettingsMapper;
    private final SocialMediaLinkMapper socialMediaLinkMapper;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<UserSettingsDto> getByUserId(Long userId) {
        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));
        return new SuccessDataResult<>(userSettingsMapper.entityToDto(settings), messageService.getMessage(Messages.SETTINGS_FOUND));
    }

    @Override
    @Transactional
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
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.isPremiumActive(),
                subscriptionService.resolvePremiumUntil(user.getId()),
                userSettingsMapper.entityToDto(settings),
                socialLinks
        );
        return new SuccessDataResult<>(overviewDto, messageService.getMessage(Messages.SETTINGS_FOUND));
    }

    @Override
    @Transactional
    public Result updateSettings(Long userId, UpdateSettingsRequest request) {
        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));

        if (request.getMessagingEnabled() != null) {
            settings.setMessagingEnabled(request.getMessagingEnabled());
        }
        if (request.getOnlineVisibilityEnabled() != null) {
            settings.setOnlineVisibilityEnabled(request.getOnlineVisibilityEnabled());
        }
        if (request.getMessageNotificationsEnabled() != null) {
            settings.setMessageNotificationsEnabled(request.getMessageNotificationsEnabled());
        }
        if (request.getFriendNotificationsEnabled() != null) {
            settings.setFriendNotificationsEnabled(request.getFriendNotificationsEnabled());
        }
        if (request.getPlateReviewNotificationsEnabled() != null) {
            settings.setPlateReviewNotificationsEnabled(request.getPlateReviewNotificationsEnabled());
        }
        if (request.getNewFollowerNotificationsEnabled() != null) {
            settings.setNewFollowerNotificationsEnabled(request.getNewFollowerNotificationsEnabled());
        }
        if (request.getReviewReplyNotificationsEnabled() != null) {
            settings.setReviewReplyNotificationsEnabled(request.getReviewReplyNotificationsEnabled());
        }
        if (request.getFollowingListVisible() != null) {
            settings.setFollowingListVisible(request.getFollowingListVisible());
        }

        userSettingsDao.save(settings);
        return new SuccessResult(messageService.getMessage(Messages.SETTINGS_UPDATED));
    }

    @Override
    @Transactional
    public Result updateAppearance(Long userId, UpdateAppearanceRequest request) {
        UserSettings settings = userSettingsDao.findByUserId(userId).orElseGet(() -> createDefaultSettings(userId));
        settings.setThemeMode(request.getThemeMode().trim().toUpperCase());
        settings.setAccentHex(request.getAccentHex() == null ? null : request.getAccentHex().trim().toUpperCase());
        userSettingsDao.save(settings);
        return new SuccessResult(messageService.getMessage(Messages.SETTINGS_UPDATED));
    }

    private UserSettings createDefaultSettings(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setMessagingEnabled(true);
        settings.setOnlineVisibilityEnabled(true);
        settings.setMessageNotificationsEnabled(true);
        settings.setFriendNotificationsEnabled(true);
        return userSettingsDao.save(settings);
    }
}

