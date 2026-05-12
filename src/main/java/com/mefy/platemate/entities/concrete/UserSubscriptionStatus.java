package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;

public enum UserSubscriptionStatus implements IEntity {
    PENDING,
    ACTIVE,
    EXPIRED,
    CANCELED
}
