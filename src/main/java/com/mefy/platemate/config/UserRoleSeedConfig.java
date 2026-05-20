package com.mefy.platemate.config;

import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserRoleDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSubscriptionDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserRole;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import com.mefy.platemate.entities.concrete.UserSubscription;
import com.mefy.platemate.entities.concrete.UserSubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class UserRoleSeedConfig {

    private final IUserRoleDao userRoleDao;
    private final IUserDao userDao;
    private final IUserSubscriptionDao userSubscriptionDao;

    @Bean
    public ApplicationRunner seedUserRoles() {
        return args -> {
            seedRoles();
            syncExistingUsersWithRoles();
        };
    }

    private void seedRoles() {
        seedRole(UserRoleCode.NORMAL, "Normal User");
        seedRole(UserRoleCode.PREMIUM, "Premium User");
        seedRole(UserRoleCode.ADMIN, "Admin User");
    }

    private void seedRole(UserRoleCode code, String name) {
        if (userRoleDao.existsByCode(code)) {
            return;
        }
        UserRole role = new UserRole();
        role.setCode(code);
        role.setName(name);
        role.setActive(true);
        userRoleDao.save(role);
    }

    private void syncExistingUsersWithRoles() {
        UserRole normalRole = userRoleDao.findByCode(UserRoleCode.NORMAL).orElse(null);
        UserRole premiumRole = userRoleDao.findByCode(UserRoleCode.PREMIUM).orElse(null);
        if (normalRole == null || premiumRole == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<User> users = userDao.findAll();
        for (User user : users) {
            if (user.hasRole(UserRoleCode.ADMIN)) {
                continue;
            }

            List<UserSubscription> subscriptions = userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(user.getId());
            boolean hasActiveNow = subscriptions.stream().anyMatch(subscription ->
                    subscription.getStatus() != UserSubscriptionStatus.CANCELED
                            && subscription.getStartedAt() != null
                            && !subscription.getStartedAt().isAfter(now)
                            && subscription.getExpiresAt() != null
                            && subscription.getExpiresAt().isAfter(now)
            );

            if (!hasActiveNow && subscriptions.isEmpty()) {
                hasActiveNow = user.getPremiumUntil() != null && user.getPremiumUntil().isAfter(now);
            }

            UserRole targetRole = hasActiveNow ? premiumRole : normalRole;

            if (user.getRole() == null || user.getRole().getCode() != targetRole.getCode()) {
                user.setRole(targetRole);
                userDao.save(user);
            }
        }
    }
}
