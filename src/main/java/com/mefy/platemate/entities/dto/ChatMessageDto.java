package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto implements IDto {
    private Long id;
    private Long chatRoomId; // So the client can route socket messages to the right room
    private Long senderUserId; // So the client can tell own messages apart
    private String senderUsername; // Who sent it?
    private String messageContent;
    private LocalDateTime sentAt;
    private boolean isRead;
    private String status; // SENT / DELIVERED / READ (for tick states)
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
}
