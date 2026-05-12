package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ISubscriptionService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.UserMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserRoleDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSubscriptionDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserRole;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import com.mefy.platemate.entities.concrete.UserSubscription;
import com.mefy.platemate.entities.concrete.UserSubscriptionStatus;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.UserSubscriptionDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionManager implements ISubscriptionService {

    private final IUserDao userDao;
    private final IUserRoleDao userRoleDao;
    private final IUserSubscriptionDao userSubscriptionDao;
    private final UserMapper userMapper;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<UserDto> activate(Long currentUserId, Integer days) {
        User user = userDao.findById(currentUserId).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        UserRole normalRole = userRoleDao.findByCode(UserRoleCode.NORMAL).orElse(null);
        UserRole premiumRole = userRoleDao.findByCode(UserRoleCode.PREMIUM).orElse(null);
        if (normalRole == null || premiumRole == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_ROLE_NOT_FOUND));
        }

        LocalDateTime now = LocalDateTime.now();
        ensureBackfillForLegacyPremiumUser(user, now);
        syncSubscriptionStatuses(user.getId(), now);

        LocalDateTime base = resolveBaseStart(user.getId(), now);
        LocalDateTime expiresAt = base.plusDays(days);

        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setPurchasedDays(days);
        subscription.setStartedAt(base);
        subscription.setExpiresAt(expiresAt);
        subscription.setStatus(base.isAfter(now) ? UserSubscriptionStatus.PENDING : UserSubscriptionStatus.ACTIVE);
        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);
        userSubscriptionDao.save(subscription);

        syncSubscriptionStatuses(user.getId(), now);
        syncRoleAndPremiumUntil(user, normalRole, premiumRole, now);

        UserDto dto = buildSubscriptionAwareUserDto(user, now);

        return new SuccessDataResult<>(
                dto,
                messageService.getMessage(Messages.SUBSCRIPTION_ACTIVATED)
        );
    }

    @Override
    @Transactional
    public DataResult<UserDto> getCurrentSubscription(Long currentUserId) {
        User user = userDao.findById(currentUserId).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        UserRole normalRole = userRoleDao.findByCode(UserRoleCode.NORMAL).orElse(null);
        UserRole premiumRole = userRoleDao.findByCode(UserRoleCode.PREMIUM).orElse(null);
        if (normalRole == null || premiumRole == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_ROLE_NOT_FOUND));
        }

        LocalDateTime now = LocalDateTime.now();
        ensureBackfillForLegacyPremiumUser(user, now);
        syncSubscriptionStatuses(user.getId(), now);
        syncRoleAndPremiumUntil(user, normalRole, premiumRole, now);

        UserDto dto = buildSubscriptionAwareUserDto(user, now);

        return new SuccessDataResult<>(
                dto,
                messageService.getMessage(Messages.SUBSCRIPTION_STATUS_FOUND)
        );
    }

    @Override
    @Transactional
    public DataResult<List<UserSubscriptionDto>> getMySubscriptionHistory(Long currentUserId) {
        User user = userDao.findById(currentUserId).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        LocalDateTime now = LocalDateTime.now();
        ensureBackfillForLegacyPremiumUser(user, now);
        syncSubscriptionStatuses(user.getId(), now);

        List<UserSubscriptionDto> history = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(this::mapToDto)
                .toList();

        return new SuccessDataResult<>(history, messageService.getMessage(Messages.SUBSCRIPTION_HISTORY_LISTED));
    }

    private LocalDateTime resolveBaseStart(Long userId, LocalDateTime now) {
        return userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(subscription -> subscription.getStatus() != UserSubscriptionStatus.CANCELED)
                .map(UserSubscription::getExpiresAt)
                .filter(expiresAt -> expiresAt != null && expiresAt.isAfter(now))
                .max(LocalDateTime::compareTo)
                .orElse(now);
    }

    private void ensureBackfillForLegacyPremiumUser(User user, LocalDateTime now) {
        List<UserSubscription> existing = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (!existing.isEmpty()) {
            return;
        }

        if (user.getPremiumUntil() == null || !user.getPremiumUntil().isAfter(now)) {
            return;
        }

        long betweenDays = ChronoUnit.DAYS.between(now, user.getPremiumUntil());
        int purchasedDays = (int) Math.max(1, betweenDays == 0 ? 1 : betweenDays);

        UserSubscription migrated = new UserSubscription();
        migrated.setUser(user);
        migrated.setPurchasedDays(purchasedDays);
        migrated.setStartedAt(now);
        migrated.setExpiresAt(user.getPremiumUntil());
        migrated.setStatus(UserSubscriptionStatus.ACTIVE);
        migrated.setCreatedAt(now);
        migrated.setUpdatedAt(now);
        userSubscriptionDao.save(migrated);
    }

    private void syncSubscriptionStatuses(Long userId, LocalDateTime now) {
        List<UserSubscription> subscriptions = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(userId);
        boolean changed = false;

        for (UserSubscription subscription : subscriptions) {
            if (subscription.getStatus() == UserSubscriptionStatus.CANCELED) {
                continue;
            }

            UserSubscriptionStatus expectedStatus = resolveExpectedStatus(subscription, now);
            if (subscription.getStatus() != expectedStatus) {
                subscription.setStatus(expectedStatus);
                subscription.setUpdatedAt(now);
                changed = true;
            }
        }

        if (changed) {
            userSubscriptionDao.saveAll(subscriptions);
        }
    }

    private UserSubscriptionStatus resolveExpectedStatus(UserSubscription subscription, LocalDateTime now) {
        if (subscription.getExpiresAt() == null || !subscription.getExpiresAt().isAfter(now)) {
            return UserSubscriptionStatus.EXPIRED;
        }

        if (subscription.getStartedAt() != null && subscription.getStartedAt().isAfter(now)) {
            return UserSubscriptionStatus.PENDING;
        }

        return UserSubscriptionStatus.ACTIVE;
    }

    private void syncRoleAndPremiumUntil(User user, UserRole normalRole, UserRole premiumRole, LocalDateTime now) {
        List<UserSubscription> subscriptions = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(user.getId());

        LocalDateTime latestFutureExpiry = subscriptions.stream()
                .filter(s -> s.getStatus() != UserSubscriptionStatus.CANCELED)
                .map(UserSubscription::getExpiresAt)
                .filter(expiresAt -> expiresAt != null && expiresAt.isAfter(now))
                .max(LocalDateTime::compareTo)
                .orElse(null);

        boolean hasActiveNow = subscriptions.stream()
                .anyMatch(s -> s.getStatus() == UserSubscriptionStatus.ACTIVE
                        && s.getStartedAt() != null
                        && s.getStartedAt().isBefore(now.plusNanos(1))
                        && s.getExpiresAt() != null
                        && s.getExpiresAt().isAfter(now));

        UserRole expectedRole = hasActiveNow ? premiumRole : normalRole;

        boolean changed = false;
        if (user.getRole() == null || user.getRole().getCode() != expectedRole.getCode()) {
            user.setRole(expectedRole);
            changed = true;
        }

        if ((user.getPremiumUntil() == null && latestFutureExpiry != null)
                || (user.getPremiumUntil() != null && !user.getPremiumUntil().equals(latestFutureExpiry))) {
            user.setPremiumUntil(latestFutureExpiry);
            changed = true;
        }

        if (changed) {
            userDao.save(user);
        }
    }

    private UserDto buildSubscriptionAwareUserDto(User user, LocalDateTime now) {
        UserDto dto = userMapper.entityToDto(user);

        List<UserSubscription> subscriptions = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(user.getId());
        UserSubscription currentSubscription = subscriptions.stream()
                .filter(s -> s.getStatus() == UserSubscriptionStatus.ACTIVE
                        && s.getStartedAt() != null
                        && !s.getStartedAt().isAfter(now)
                        && s.getExpiresAt() != null
                        && s.getExpiresAt().isAfter(now))
                .max(Comparator.comparing(UserSubscription::getExpiresAt))
                .orElseGet(() -> subscriptions.stream().findFirst().orElse(null));

        if (currentSubscription != null) {
            dto.setCurrentSubscriptionStartedAt(currentSubscription.getStartedAt());
            dto.setCurrentSubscriptionExpiresAt(currentSubscription.getExpiresAt());
            dto.setCurrentSubscriptionPurchasedDays(currentSubscription.getPurchasedDays());
            dto.setCurrentSubscriptionStatus(currentSubscription.getStatus().name());
        }

        return dto;
    }

    private UserSubscriptionDto mapToDto(UserSubscription subscription) {
        UserSubscriptionDto dto = new UserSubscriptionDto();
        dto.setId(subscription.getId());
        dto.setPurchasedDays(subscription.getPurchasedDays());
        dto.setStatus(subscription.getStatus().name());
        dto.setStartedAt(subscription.getStartedAt());
        dto.setExpiresAt(subscription.getExpiresAt());
        dto.setCreatedAt(subscription.getCreatedAt());
        dto.setUpdatedAt(subscription.getUpdatedAt());
        return dto;
    }
}
