package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IParticipantService;
import com.mefy.platemate.core.utilities.results.DataResult;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantManager implements IParticipantService {

    private final IParticipantDao participantDao;
    private final IUserDao userDao;

    @Override
    public Result add(Participant participant) {
        participantDao.save(participant);
        return new SuccessResult();
    }

    @Override
    public Result addParticipantToRoom(ChatRoom room, Long userId) {
        User user = userDao.findById(userId).orElseThrow();
        Participant participant = new Participant();
        participant.setChatRoom(room);
        participant.setUser(user);
        participant.setJoinedAt(LocalDateTime.now());
        participantDao.save(participant);
        return new SuccessResult();
    }

    @Override
    public Result delete(Long id) {
        participantDao.deleteById(id);
        return new SuccessResult();
    }

    @Override
    public DataResult<List<Participant>> getByUserId(Long userId) {
        return new SuccessDataResult<>(participantDao.findByUserId(userId));
    }

    @Override
    public DataResult<Optional<ChatRoom>> findPrivateChatBetweenUsers(Long u1, Long u2) {
        return new SuccessDataResult<>(participantDao.findPrivateChatBetweenUsers(u1, u2));
    }
}
