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
    private String roomName; // Group name if it's a group, empty otherwise
    private boolean isGroup;
    private LocalDateTime lastMessageAt;
    private String lastMessageContent; // For displaying a short preview in the list
    private String otherParticipantName; // Name of the other person in 1-1 chats
}
