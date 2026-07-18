package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper implements IMapper<ChatMessage, ChatMessageDto> {
    @Override
    public ChatMessageDto entityToDto(ChatMessage entity) {
        if (entity == null) return null;
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(entity.getId());
        if (entity.getChatRoom() != null) {
            dto.setChatRoomId(entity.getChatRoom().getId());
        }
        dto.setMessageContent(entity.getContent());
        dto.setSentAt(entity.getSentAt());
        dto.setRead(entity.isRead());
        dto.setStatus(entity.getStatus() == null ? null : entity.getStatus().name());
        dto.setDeliveredAt(entity.getDeliveredAt());
        dto.setReadAt(entity.getReadAt());
        dto.setClientMessageId(entity.getClientMessageId());

        if (entity.getSender() != null) {
            dto.setSenderUserId(entity.getSender().getId());
            dto.setSenderUsername(entity.getSender().getUsername());
        }
        return dto;
    }

    @Override
    public ChatMessage dtoToEntity(ChatMessageDto dto) {
        return null;
    }
}


