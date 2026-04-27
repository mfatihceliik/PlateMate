package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper implements ModelMapperService<ChatMessage, ChatMessageDto> {
    @Override
    public ChatMessageDto entityToDto(ChatMessage entity) {
        if (entity == null) return null;
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(entity.getId());
        dto.setMessageContent(entity.getContent());
        dto.setSentAt(entity.getSentAt());
        dto.setRead(entity.isRead());

        if (entity.getSender() != null) {
            dto.setSenderUsername(entity.getSender().getUsername());
        }
        return dto;
    }

    @Override
    public ChatMessage dtoToEntity(ChatMessageDto dto) {
        return null;
    }
}
