package com.mefy.platemate.api.websocket;

import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * On WebSocket real-time messaging.
 *
 * Flow:
 * 1. Client → /app/chat.send (STOMP SEND)
 * 2. Server → DB'ye kaydet (IChatMessageService)
 * 3. Server → /topic/room/{roomId}'e broadcast (SimpMessagingTemplate)
 * 4. Subscribe olan tüm client'lar mesajı alır
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final IChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        // Session'dan userId'yi al (WebSocket auth ile set edilecek)
        // Şimdilik header'dan alıyoruz
        String userIdStr = headerAccessor.getFirstNativeHeader("userId");
        if (userIdStr == null) return;

        Long senderId = Long.parseLong(userIdStr);

        User sender = new User();
        sender.setId(senderId);

        ChatRoom room = new ChatRoom();
        room.setId(request.getChatRoomId());

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setChatRoom(room);
        message.setContent(request.getContent());

        // DB'ye kaydet
        DataResult<ChatMessageDto> result = chatMessageService.sendMessage(message);

        if (result.isSuccess()) {
            // Odadaki herkese broadcast et
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getChatRoomId(),
                    result.getData()
            );
        }
    }
}
