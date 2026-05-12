package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IUserSubscriptionDao extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(Long userId);
}
