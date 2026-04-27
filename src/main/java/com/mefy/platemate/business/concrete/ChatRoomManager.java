package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IChatRoomService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.ChatRoomMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.dataAccess.abstracts.IChatRoomDao;
import com.mefy.platemate.dataAccess.abstracts.IParticipantDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.ChatRoomDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomManager implements IChatRoomService {

    private final IChatRoomDao chatRoomDao;
    private final IParticipantDao participantDao;
    private final ChatRoomMapper chatRoomMapper;
    private final IUserDao userDao;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<ChatRoomDto> getOrCreateChatRoom(Long userOneId, Long userTwoId) {
        ChatRoom existingRoom = findPrivateChatBetweenUsers(userOneId, userTwoId);

        if (existingRoom != null) {
            return new SuccessDataResult<>(chatRoomMapper.entityToDto(existingRoom), messageService.getMessage(Messages.CHAT_ROOM_FOUND));
        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setGroup(false);
        newRoom.setCreatedAt(LocalDateTime.now());
        chatRoomDao.save(newRoom);

        addParticipantToRoom(newRoom, userOneId);
        addParticipantToRoom(newRoom, userTwoId);

        return new SuccessDataResult<>(chatRoomMapper.entityToDto(newRoom), messageService.getMessage(Messages.CHAT_ROOM_CREATED));
    }

    @Override
    public DataResult<List<ChatRoomDto>> getUserRooms(Long userId) {
        List<Participant> participations = participantDao.findByUserId(userId);
        List<ChatRoomDto> dtos = participations.stream()
                .map(p -> chatRoomMapper.entityToDto(p.getChatRoom()))
                .collect(Collectors.toList());

        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.USER_ROOMS_LISTED));
    }

    private void addParticipantToRoom(ChatRoom room, Long userId) {
        User user = userDao.findById(userId).orElseThrow();
        Participant participant = new Participant();
        participant.setChatRoom(room);
        participant.setUser(user);
        participant.setJoinedAt(LocalDateTime.now());
        participantDao.save(participant);
    }

    private ChatRoom findPrivateChatBetweenUsers(Long id1, Long id2) {
        return participantDao.findPrivateChatBetweenUsers(id1, id2).orElse(null);
    }
}
