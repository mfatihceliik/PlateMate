package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;

import java.util.List;
import java.util.Optional;

public interface IParticipantService {
    Result addParticipantToRoom(Long roomId, Long userId);
    Result delete(Long id);
    DataResult<List<Long>> getByUserId(Long userId);
    DataResult<Long> findPrivateChatBetweenUsers(Long u1, Long u2);
    boolean isRoomMember(Long userId, Long roomId);
}
