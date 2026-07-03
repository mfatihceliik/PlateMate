package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;

public interface IFollowService {

    Result follow(Long followerId, Long followingId);

    Result unfollow(Long followerId, Long followingId);

    long countFollowers(Long userId);

    long countFollowing(Long userId);

    boolean isFollowing(Long followerId, Long followingId);
}
