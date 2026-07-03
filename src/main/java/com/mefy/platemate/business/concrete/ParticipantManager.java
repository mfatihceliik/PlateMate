package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IParticipantService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IParticipantDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;
import com.mefy.platemate.entities.concrete.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantManager implements IParticipantService {

    private final IParticipantDao participantDao;
    private final IUserDao userDao;
    private final IMessageService messageService;



    @Override
    @Transactional
    public Result addParticipantToRoom(Long roomId, Long userId) {
        User user = userDao.findById(userId).orElse(null);
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        
        // Let's create an entity reference to ChatRoom to save it
        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        
        Participant participant = new Participant();
        participant.setChatRoom(room);
        participant.setUser(user);
        participant.setJoinedAt(LocalDateTime.now());
        participantDao.save(participant);
        return new SuccessResult();
    }

    @Override
    @Transactional
    public Result removeFromRoom(Long roomId, Long userId) {
        // Soft-hide only: the participant row (membership) is preserved so the user keeps
        // receiving future messages/socket pushes for this room; it just drops out of their
        // own room list until unhideForUser() clears hiddenAt (e.g. the other side messages again).
        Participant participant = participantDao.findByUserIdAndChatRoomId(userId, roomId).orElse(null);
        if (participant != null) {
            participant.setHiddenAt(LocalDateTime.now());
            participantDao.save(participant);
        }
        return new SuccessResult();
    }

    @Override
    @Transactional
    public void unhideForUser(Long roomId, Long userId) {
        Participant participant = participantDao.findByUserIdAndChatRoomId(userId, roomId).orElse(null);
        if (participant != null && participant.getHiddenAt() != null) {
            participant.setHiddenAt(null);
            participantDao.save(participant);
        }
    }

    @Override
    @Transactional
    public Result delete(Long id) {
        participantDao.deleteById(id);
        return new SuccessResult();
    }

    @Override
    public DataResult<List<Long>> getByUserId(Long userId) {
        List<Long> roomIds = participantDao.findByUserIdAndHiddenAtIsNull(userId).stream()
                .map(p -> p.getChatRoom().getId())
                .collect(java.util.stream.Collectors.toList());
        return new SuccessDataResult<>(roomIds);
    }

    @Override
    public DataResult<Long> findPrivateChatBetweenUsers(Long u1, Long u2) {
        return new SuccessDataResult<>(participantDao.findPrivateChatBetweenUsers(u1, u2)
                .map(ChatRoom::getId)
                .orElse(null));
    }

    @Override
    public boolean isRoomMember(Long userId, Long roomId) {
        return participantDao.existsByUserIdAndChatRoomId(userId, roomId);
    }
}
