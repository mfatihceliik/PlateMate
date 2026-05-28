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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
        syncRoleFromSubscriptions(user, normalRole, premiumRole, now);

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
        syncSubscriptionStatuses(user.getId(), now);
        syncRoleFromSubscriptions(user, normalRole, premiumRole, now);

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

    private void syncRoleFromSubscriptions(User user, UserRole normalRole, UserRole premiumRole, LocalDateTime now) {
        List<UserSubscription> subscriptions = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(user.getId());

        boolean hasActiveNow = subscriptions.stream()
                .anyMatch(s -> s.getStatus() == UserSubscriptionStatus.ACTIVE
                        && s.getStartedAt() != null
                        && s.getStartedAt().isBefore(now.plusNanos(1))
                        && s.getExpiresAt() != null
                        && s.getExpiresAt().isAfter(now));

        UserRole expectedRole = hasActiveNow ? premiumRole : normalRole;

        boolean changed = false;
        if (!user.hasRole(UserRoleCode.ADMIN)
                && (user.getRole() == null || !Objects.equals(user.getRole().getCodeId(), expectedRole.getCodeId()))) {
            user.setRole(expectedRole);
            changed = true;
        }

        if (changed) {
            userDao.save(user);
        }
    }

    private UserDto buildSubscriptionAwareUserDto(User user, LocalDateTime now) {
        UserDto dto = userMapper.entityToDto(user);

        List<UserSubscription> subscriptions = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(user.getId());
        dto.setPremiumUntil(resolveLatestFutureExpiry(subscriptions, now));
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
            dto.setCurrentSubscriptionStatusId(currentSubscription.getStatusId());
            dto.setCurrentSubscriptionStatusCode(currentSubscription.getStatusCode());
        }

        return dto;
    }

    private LocalDateTime resolveLatestFutureExpiry(List<UserSubscription> subscriptions, LocalDateTime now) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }
        return subscriptions.stream()
                .filter(s -> s.getStatus() != UserSubscriptionStatus.CANCELED)
                .map(UserSubscription::getExpiresAt)
                .filter(expiresAt -> expiresAt != null && expiresAt.isAfter(now))
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private UserSubscriptionDto mapToDto(UserSubscription subscription) {
        UserSubscriptionDto dto = new UserSubscriptionDto();
        dto.setId(subscription.getId());
        dto.setPurchasedDays(subscription.getPurchasedDays());
        dto.setStatusId(subscription.getStatusId());
        dto.setStatusCode(subscription.getStatusCode());
        dto.setStartedAt(subscription.getStartedAt());
        dto.setExpiresAt(subscription.getExpiresAt());
        dto.setCreatedAt(subscription.getCreatedAt());
        dto.setUpdatedAt(subscription.getUpdatedAt());
        return dto;
    }
}
