package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.FollowListItemDto;

import java.util.List;

public interface IFollowService {

    Result follow(Long followerId, Long followingId);

    Result unfollow(Long followerId, Long followingId);

    long countFollowers(Long userId);

    long countFollowing(Long userId);

    boolean isFollowing(Long followerId, Long followingId);

    DataResult<List<FollowListItemDto>> getFollowers(Long userId, Long requesterId);

    DataResult<List<FollowListItemDto>> getFollowing(Long userId, Long requesterId);
}
