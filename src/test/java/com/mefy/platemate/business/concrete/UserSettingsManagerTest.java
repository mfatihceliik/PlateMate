package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.SocialMediaLinkMapper;
import com.mefy.platemate.core.utilities.mappers.UserSettingsMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSubscriptionDao;
import com.mefy.platemate.entities.concrete.SocialMediaLink;
import com.mefy.platemate.entities.concrete.SocialPlatform;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.concrete.UserRole;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.concrete.UserSubscription;
import com.mefy.platemate.entities.concrete.UserSubscriptionStatus;
import com.mefy.platemate.entities.dto.UserSettingsOverviewDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingsManagerTest {

    @Mock
    private IUserSettingsDao userSettingsDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private IUserProfileDao userProfileDao;
    @Mock
    private IUserSubscriptionDao userSubscriptionDao;
    @Mock
    private IMessageService messageService;

    private UserSettingsManager userSettingsManager;

    @BeforeEach
    void setUp() {
        userSettingsManager = new UserSettingsManager(
                userSettingsDao,
                userDao,
                userProfileDao,
                userSubscriptionDao,
                new UserSettingsMapper(),
                new SocialMediaLinkMapper(),
                messageService
        );
    }

    @Test
    void getOverviewByUserIdReturnsComposedOverview() {
        Long userId = 5L;
        User user = buildPremiumUser(userId);

        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setMessagingEnabled(true);
        settings.setMessageNotificationsEnabled(false);
        settings.setFriendNotificationsEnabled(true);

        SocialMediaLink link = new SocialMediaLink();
        link.setPlatform(SocialPlatform.INSTAGRAM);
        link.setUrl("https://instagram.com/fatih");

        UserProfile profile = new UserProfile();
        profile.setId(userId);
        profile.setUser(user);
        profile.setSocialMediaLinks(List.of(link));

        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsDao.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(userProfileDao.findByIdWithSocialMediaLinks(userId)).thenReturn(Optional.of(profile));
        when(userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(buildActiveSubscription(user)));
        when(messageService.getMessage(Messages.SETTINGS_FOUND)).thenReturn("settings-found");

        DataResult<UserSettingsOverviewDto> result = userSettingsManager.getOverviewByUserId(userId);

        assertTrue(result.isSuccess());
        assertEquals("settings-found", result.getMessage());
        assertNotNull(result.getData());
        assertEquals("fatih@platemate.test", result.getData().getEmail());
        assertTrue(result.getData().isPremiumActive());
        assertNotNull(result.getData().getPremiumUntil());
        assertNotNull(result.getData().getUserSettings());
        assertTrue(result.getData().getUserSettings().isMessagingEnabled());
        assertFalse(result.getData().getUserSettings().isMessageNotificationsEnabled());
        assertEquals(1, result.getData().getSocialMediaLinks().size());
        assertEquals(SocialPlatform.INSTAGRAM, result.getData().getSocialMediaLinks().get(0).getPlatform());
    }

    @Test
    void getOverviewByUserIdCreatesDefaultSettingsWhenMissing() {
        Long userId = 7L;
        User user = buildPremiumUser(userId);

        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(userSettingsDao.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSettingsDao.save(any(UserSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileDao.findByIdWithSocialMediaLinks(userId)).thenReturn(Optional.empty());
        when(userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(buildActiveSubscription(user)));
        when(messageService.getMessage(Messages.SETTINGS_FOUND)).thenReturn("settings-found");

        DataResult<UserSettingsOverviewDto> result = userSettingsManager.getOverviewByUserId(userId);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().getUserSettings().isMessagingEnabled());
        assertTrue(result.getData().getUserSettings().isMessageNotificationsEnabled());
        assertTrue(result.getData().getUserSettings().isFriendNotificationsEnabled());
        assertTrue(result.getData().getSocialMediaLinks().isEmpty());

        ArgumentCaptor<UserSettings> captor = ArgumentCaptor.forClass(UserSettings.class);
        verify(userSettingsDao).save(captor.capture());
        assertEquals(userId, captor.getValue().getUser().getId());
    }

    @Test
    void getOverviewByUserIdReturnsErrorWhenUserMissing() {
        Long userId = 99L;
        when(userDao.findById(userId)).thenReturn(Optional.empty());
        when(messageService.getMessage(Messages.USER_NOT_FOUND)).thenReturn("user-not-found");

        DataResult<UserSettingsOverviewDto> result = userSettingsManager.getOverviewByUserId(userId);

        assertFalse(result.isSuccess());
        assertEquals("user-not-found", result.getMessage());
    }

    private User buildPremiumUser(Long userId) {
        UserRole role = new UserRole();
        role.setCode(UserRoleCode.PREMIUM);

        User user = new User();
        user.setId(userId);
        user.setEmail("fatih@platemate.test");
        user.setRole(role);
        return user;
    }

    private UserSubscription buildActiveSubscription(User user) {
        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setStatus(UserSubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now().minusDays(1));
        subscription.setExpiresAt(LocalDateTime.now().plusDays(10));
        subscription.setPurchasedDays(30);
        return subscription;
    }
}
