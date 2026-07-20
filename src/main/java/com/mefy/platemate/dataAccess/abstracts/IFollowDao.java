package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFollowDao extends JpaRepository<Follow, Long> {

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Kim X'i takip ediyor (X'in takipçileri)
    List<Follow> findByFollowingId(Long followingId);

    // X kimi takip ediyor (X'in takip ettikleri)
    List<Follow> findByFollowerId(Long followerId);
}
