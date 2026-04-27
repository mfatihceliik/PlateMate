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

        // Son mesajın önizlemesi için (Eğer mesajlar yüklendiyse)
        if (entity.getMessages() != null && !entity.getMessages().isEmpty()) {
            dto.setLastMessageContent(entity.getMessages().get(entity.getMessages().size() - 1).getContent());
        }

        // 1-1 sohbetlerde karşıdaki kişinin adını göster
        // Not: Bu metot çağrılırken current user bilgisi gerekebilir,
        // şimdilik ilk katılımcının adını set ediyoruz.
        // API katmanında currentUserId ile filtreleme yapılacak.
        if (!entity.isGroup() && entity.getParticipants() != null) {
            entity.getParticipants().stream()
                    .findFirst()
                    .map(Participant::getUser)
                    .ifPresent(user -> dto.setOtherParticipantName(user.getUsername()));
        }

        return dto;
    }

    @Override
    public ChatRoom dtoToEntity(ChatRoomDto dto) { return null; }
}
