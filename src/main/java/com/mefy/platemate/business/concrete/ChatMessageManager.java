package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.ChatMessageMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IChatMessageDao;
import com.mefy.platemate.dataAccess.abstracts.IChatRoomDao;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageManager implements IChatMessageService {

    private final IChatMessageDao chatMessageDao;
    private final IChatRoomDao chatRoomDao;
    private final ChatMessageMapper chatMessageMapper;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<ChatMessageDto> sendMessage(ChatMessage message) {
        chatMessageDao.save(message);

        ChatRoom room = chatRoomDao.findById(message.getChatRoom().getId()).orElseThrow();
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomDao.save(room);

        ChatMessageDto dto = chatMessageMapper.entityToDto(message);
        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.MESSAGE_SENT));
    }

    @Override
    public DataResult<List<ChatMessageDto>> getMessagesByRoomId(Long roomId) {
        List<ChatMessage> messages = chatMessageDao.findByChatRoomIdOrderBySentAtAsc(roomId);
        List<ChatMessageDto> dtos = messages.stream()
                .map(chatMessageMapper::entityToDto)
                .collect(Collectors.toList());

        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.MESSAGES_LISTED));
    }

    @Override
    @Transactional
    public Result markAsRead(Long roomId, Long userId) {
        List<ChatMessage> unreadMessages = chatMessageDao.findByChatRoomIdOrderBySentAtAsc(roomId);

        unreadMessages.stream()
                .filter(m -> !m.getSender().getId().equals(userId) && !m.isRead())
                .forEach(m -> m.setRead(true));

        chatMessageDao.saveAll(unreadMessages);
        return new SuccessResult(messageService.getMessage("messages.read"));
    }
}
