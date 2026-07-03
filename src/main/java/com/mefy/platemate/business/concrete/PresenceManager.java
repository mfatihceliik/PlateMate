package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPresenceService;
import com.mefy.platemate.business.abstracts.ISocketPushService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.constants.SocketEvents;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.dataAccess.abstracts.IParticipantDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.entities.concrete.UserSettings;
import com.mefy.platemate.entities.dto.PresenceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresenceManager implements IPresenceService {

    private final IParticipantDao participantDao;
    private final IUserSettingsDao userSettingsDao;
    private final ISocketPushService socketPushService;
    private final IMessageService messageService;

    @Override
    public boolean canSeePresence(Long viewerId, Long targetId) {
        if (viewerId == null || targetId == null) {
            return false;
        }
        // Reciprocal: hiding your own status also hides everyone else's from you.
        return isVisibilityEnabled(viewerId) && isVisibilityEnabled(targetId);
    }

    @Override
    public boolean isOnlineFor(Long viewerId, Long targetId) {
        return canSeePresence(viewerId, targetId) && socketPushService.isUserOnline(targetId);
    }

    @Override
    public void broadcastPresence(Long userId, boolean online) {
        if (userId == null) {
            return;
        }
        String event = online ? SocketEvents.PRESENCE_ONLINE : SocketEvents.PRESENCE_OFFLINE;
        PresenceDto payload = new PresenceDto(userId, online);
        participantDao.findPrivateChatPartnerIds(userId).forEach(partnerId -> {
            if (canSeePresence(partnerId, userId)) {
                socketPushService.sendToUser(partnerId, event, payload);
            }
        });
    }

    @Override
    public DataResult<PresenceDto> getRoomPresence(Long roomId, Long currentUserId) {
        if (roomId == null || currentUserId == null
                || !participantDao.existsByUserIdAndChatRoomId(currentUserId, roomId)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.AUTH_UNAUTHORIZED));
        }

        List<Long> otherIds = participantDao.findOtherParticipantIds(roomId, currentUserId);
        Long otherId = otherIds.isEmpty() ? null : otherIds.get(0);
        boolean online = isOnlineFor(currentUserId, otherId);
        return new SuccessDataResult<>(new PresenceDto(otherId, online));
    }

    private boolean isVisibilityEnabled(Long userId) {
        return userSettingsDao.findByUserId(userId)
                .map(UserSettings::isOnlineVisibilityEnabled)
                .orElse(true); // default: visible
    }
}
