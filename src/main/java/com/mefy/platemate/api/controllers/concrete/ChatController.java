package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IChatController;

import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.business.abstracts.IChatRoomService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.ChatRoomDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController implements IChatController {

    private final IChatRoomService chatRoomService;
    private final IChatMessageService chatMessageService;

    @Override
    public ResponseEntity<DataResult<List<ChatRoomDto>>> getUserRooms(
            @RequestAttribute("userId") Long currentUserId
    ) {
        return ResponseEntity.ok(chatRoomService.getUserRooms(currentUserId));
    }

    @Override
    public ResponseEntity<DataResult<ChatRoomDto>> getOrCreateRoom(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam Long otherUserId
    ) {
        DataResult<ChatRoomDto> result = chatRoomService.getOrCreateChatRoom(currentUserId, otherUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<DataResult<List<ChatMessageDto>>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatMessageService.getMessagesByRoomId(roomId));
    }

    // REST ile mesaj gönderme (Socket alternatifi — offline fallback)
    @Override
    public ResponseEntity<?> sendMessage(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody SendMessageRequest request
    ) {

        User sender = new User();
        sender.setId(currentUserId);

        ChatRoom room = new ChatRoom();
        room.setId(request.getChatRoomId());

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setChatRoom(room);
        message.setContent(request.getContent());

        DataResult<ChatMessageDto> result = chatMessageService.sendMessage(message);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<Result> markAsRead(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    ) {
        return ResponseEntity.ok(chatMessageService.markAsRead(roomId, currentUserId));
    }
}
