package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.FriendshipDto;

import java.util.List;

public interface IFriendshipService {
    Result sendRequest(Long requesterId, Long addresseeId);
    Result acceptRequest(Long friendshipId, Long currentUserId);
    Result rejectRequest(Long friendshipId, Long currentUserId);
    Result removeFriend(Long friendshipId, Long currentUserId);
    DataResult<List<FriendshipDto>> getFriends(Long userId);
    DataResult<List<FriendshipDto>> getPendingRequests(Long userId);
}
