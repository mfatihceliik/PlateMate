package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;

public interface IUserBlockService {

    Result blockUser(Long blockerId, Long blockedId);

    Result unblockUser(Long blockerId, Long blockedId);

    boolean isBlockedEitherWay(Long userId1, Long userId2);
}
