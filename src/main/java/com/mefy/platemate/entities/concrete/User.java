package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Participant> participants;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PlateReview> plateReviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserSubscription> subscriptions;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSettings settings;

    @Transient
    private LocalDateTime premiumUntil;

    public boolean hasRole(UserRoleCode roleCode) {
        if (role == null || roleCode == null) {
            return false;
        }
        return roleCode.getId().equals(role.getCodeId());
    }

    public boolean isPremiumActive() {
        return hasRole(UserRoleCode.PREMIUM);
    }

    public boolean isSubscriptionActive() {
        return isPremiumActive();
    }

    @Transient
    public LocalDateTime getPremiumUntil() {
        if (premiumUntil != null) {
            return premiumUntil;
        }
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return subscriptions.stream()
                .filter(subscription -> subscription.getStatus() != UserSubscriptionStatus.CANCELED)
                .map(UserSubscription::getExpiresAt)
                .filter(expiresAt -> expiresAt != null && expiresAt.isAfter(now))
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public void setPremiumUntil(LocalDateTime premiumUntil) {
        this.premiumUntil = premiumUntil;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
