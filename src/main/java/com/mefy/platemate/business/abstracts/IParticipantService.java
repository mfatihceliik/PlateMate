package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;

import java.util.List;
import java.util.Optional;

public interface IParticipantService {
    Result add(Participant participant);
    Result addParticipantToRoom(ChatRoom room, Long userId);
    Result delete(Long id);
    DataResult<List<Participant>> getByUserId(Long userId);
    DataResult<Optional<ChatRoom>> findPrivateChatBetweenUsers(Long u1, Long u2);
}
