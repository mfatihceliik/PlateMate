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
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<List<ChatMessageDto>>> getMessages(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    ) {
        DataResult<List<ChatMessageDto>> result = chatMessageService.getMessagesByRoomId(roomId, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // REST ile mesaj gönderme (Socket alternatifi — offline fallback)
    @Override
    public ResponseEntity<DataResult<ChatMessageDto>> sendMessage(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        DataResult<ChatMessageDto> result = chatMessageService.sendMessage(request, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> markAsRead(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result result = chatMessageService.markAsRead(roomId, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
