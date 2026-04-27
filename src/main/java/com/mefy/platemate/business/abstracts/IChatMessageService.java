package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.dto.ChatMessageDto;

import java.util.List;

public interface IChatMessageService {
    DataResult<ChatMessageDto> sendMessage(ChatMessage message); // DTO döner (WebSocket broadcast için)
    DataResult<List<ChatMessageDto>> getMessagesByRoomId(Long roomId);
    Result markAsRead(Long roomId, Long userId); // Mesajları okundu olarak işaretle
}
