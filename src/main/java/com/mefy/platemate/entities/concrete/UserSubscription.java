package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class UserSubscription implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status_id", nullable = false)
    private Long statusId = UserSubscriptionStatus.PENDING.getId();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private UserSubscriptionStatusLookup statusRef;

    @Column(nullable = false)
    private Integer purchasedDays;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Transient
    public UserSubscriptionStatus getStatus() {
        UserSubscriptionStatus fromId = UserSubscriptionStatus.fromId(statusId);
        if (fromId != null) {
            return fromId;
        }
        return UserSubscriptionStatus.fromCode(resolveStatusCodeFromRef());
    }

    public void setStatus(UserSubscriptionStatus status) {
        this.statusId = status == null ? null : status.getId();
    }

    @Transient
    public String getStatusCode() {
        UserSubscriptionStatus status = UserSubscriptionStatus.fromId(statusId);
        if (status != null) {
            return status.getCode();
        }
        return resolveStatusCodeFromRef();
    }

    private String resolveStatusCodeFromRef() {
        if (statusRef == null || !Hibernate.isInitialized(statusRef)) {
            return null;
        }
        return statusRef.getCode();
    }
}
