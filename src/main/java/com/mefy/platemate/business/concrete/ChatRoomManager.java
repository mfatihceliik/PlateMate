package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IChatRoomService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.ChatRoomMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.dataAccess.abstracts.IChatRoomDao;
import com.mefy.platemate.business.abstracts.IParticipantService;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;
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
    private final IParticipantService participantService;
    private final ChatRoomMapper chatRoomMapper;
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
        List<Participant> participations = participantService.getByUserId(userId).getData();
        List<ChatRoomDto> dtos = participations.stream()
                .map(p -> chatRoomMapper.entityToDto(p.getChatRoom()))
                .collect(Collectors.toList());

        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.USER_ROOMS_LISTED));
    }

    private void addParticipantToRoom(ChatRoom room, Long userId) {
        participantService.addParticipantToRoom(room, userId);
    }

    private ChatRoom findPrivateChatBetweenUsers(Long id1, Long id2) {
        return participantService.findPrivateChatBetweenUsers(id1, id2).getData().orElse(null);
    }
}
