package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pushed to every room participant (sender included, for their other sessions) when a
 * message is soft-deleted, so clients can live-tombstone it without a REST refetch.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDeletedDto implements IDto {
    private Long chatRoomId;
    private Long messageId;
}
