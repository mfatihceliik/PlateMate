package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper implements IMapper<ChatMessage, ChatMessageDto> {

    private static final int REPLY_PREVIEW_MAX_LENGTH = 120;

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

        ChatMessage replyTo = entity.getReplyToMessage();
        if (replyTo != null) {
            dto.setReplyToMessageId(replyTo.getId());
            if (replyTo.getSender() != null) {
                dto.setReplyToSenderUsername(replyTo.getSender().getUsername());
            }
            dto.setReplyToContentPreview(truncate(replyTo.getContent()));
        }
        return dto;
    }

    private String truncate(String content) {
        if (content == null) return null;
        if (content.length() <= REPLY_PREVIEW_MAX_LENGTH) return content;
        return content.substring(0, REPLY_PREVIEW_MAX_LENGTH);
    }

    @Override
    public ChatMessage dtoToEntity(ChatMessageDto dto) {
        return null;
    }
}


