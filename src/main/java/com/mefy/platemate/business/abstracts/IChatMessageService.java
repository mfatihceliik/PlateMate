package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;

import java.util.List;

public interface IChatMessageService {
    DataResult<ChatMessageDto> sendMessage(ChatMessage message, Long currentUserId); // DTO döner (WebSocket broadcast için)
    DataResult<ChatMessageDto> sendMessage(SendMessageRequest request, Long currentUserId);
    DataResult<List<ChatMessageDto>> getMessagesByRoomId(Long roomId, Long currentUserId);
    Result markAsRead(Long roomId, Long currentUserId); // Mesajları okundu olarak işaretle
}
