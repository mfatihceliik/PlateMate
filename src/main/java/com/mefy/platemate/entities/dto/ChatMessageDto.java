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
    private String senderUsername; // Kim gönderdi?
    private String messageContent;
    private LocalDateTime sentAt;
    private boolean isRead;
}
