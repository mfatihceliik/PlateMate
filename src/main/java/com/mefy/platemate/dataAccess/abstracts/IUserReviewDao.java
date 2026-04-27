package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserReview;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserReviewDao extends JpaRepository<UserReview, Long> {
    Page<UserReview> findByTargetProfileId(Long targetProfileId, Pageable pageable);
}
