package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;
import com.mefy.platemate.entities.dto.ChatRoomDto;
import org.springframework.stereotype.Component;

@Component
public class ChatRoomMapper implements ModelMapperService<ChatRoom, ChatRoomDto> {
    @Override
    public ChatRoomDto entityToDto(ChatRoom entity) {
        if (entity == null) return null;
        ChatRoomDto dto = new ChatRoomDto();
        dto.setId(entity.getId());
        dto.setRoomName(entity.getRoomName());
        dto.setGroup(entity.isGroup());
        dto.setLastMessageAt(entity.getLastMessageAt());

        // For the preview of the last message (if messages are loaded)
        if (entity.getMessages() != null && !entity.getMessages().isEmpty()) {
            dto.setLastMessageContent(entity.getMessages().get(entity.getMessages().size() - 1).getContent());
        }

        // In 1-1 chats, show the other person's name
        // Note: This method might need current user info when called,
        // for now we set the name of the first participant.
        // Filtering by currentUserId will be done in the API layer.
        if (!entity.isGroup() && entity.getParticipants() != null) {
            entity.getParticipants().stream()
                    .findFirst()
                    .map(Participant::getUser)
                    .ifPresent(user -> dto.setOtherParticipantName(user.getUsername()));
        }

        return dto;
    }

    @Override
    public ChatRoom dtoToEntity(ChatRoomDto dto) {
        return null;
    }
}
