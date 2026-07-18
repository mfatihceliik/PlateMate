package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;

import java.util.List;

public interface IChatMessageService {
    DataResult<ChatMessageDto> sendMessage(SendMessageRequest request, Long currentUserId);
    DataResult<List<ChatMessageDto>> getMessagesByRoomId(Long roomId, Long currentUserId);
    Result markAsRead(Long roomId, Long currentUserId); // Mark messages as read
    void markPendingDelivered(Long userId); // On (re)connect: mark incoming SENT messages as DELIVERED
    Result deleteMessage(Long messageId, Long currentUserId); // Soft-delete for everyone (sender only)
}
