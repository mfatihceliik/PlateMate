package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PresenceDto;

public interface IPresenceService {

    // Whether viewer is allowed to see target's online status (reciprocal visibility).
    boolean canSeePresence(Long viewerId, Long targetId);

    // Target's online status as visible to viewer (false if not visible or offline).
    boolean isOnlineFor(Long viewerId, Long targetId);

    // Notify the user's 1-1 chat partners that they came online / went offline.
    void broadcastPresence(Long userId, boolean online);

    // Snapshot of the other participant's presence for the given room (viewer = currentUserId).
    DataResult<PresenceDto> getRoomPresence(Long roomId, Long currentUserId);
}
