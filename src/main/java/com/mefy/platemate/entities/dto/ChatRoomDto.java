package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDto implements IDto {
    private Long id;
    private String roomName; // Grup ise grup ismi, değilse boş
    private boolean isGroup;
    private LocalDateTime lastMessageAt;
    private String lastMessageContent; // Listede küçük bir önizleme göstermek için
    private String otherParticipantName; // 1-1 konuşmalarda karşıdaki kişinin adı
}
