package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight signal emitted to a message sender when the status of their
 * message(s) changes (delivered / read). messageId is null for room-wide
 * updates (e.g. all messages in a room marked read).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageStatusSignalDto implements IDto {
    private Long roomId;
    private Long messageId;
    private String status; // DELIVERED / READ
}
