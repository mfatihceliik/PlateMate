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
    private Long otherParticipantId; // Id of the other person in 1-1 chats (for block/profile)
    private String requestStatus; // PENDING / ACCEPTED / DECLINED (message-request flow)
    private Long initiatorId; // Who started the conversation (must wait for approval)
    private int unreadCount; // Messages in this room not sent by me and not yet READ
    private Long lastMessageSenderId; // Sender of the last message in the room
}
