package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.ChatRoomDto;
import com.mefy.platemate.entities.dto.PresenceDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/chat")
public interface IChatController {

    @GetMapping("/rooms")
    ResponseEntity<DataResult<List<ChatRoomDto>>> getUserRooms(@RequestAttribute("userId") Long currentUserId);

    @PostMapping("/rooms")
    ResponseEntity<DataResult<ChatRoomDto>> getOrCreateRoom(
            @RequestAttribute("userId") Long currentUserId,
            @RequestParam Long otherUserId
    );

    @GetMapping("/rooms/{roomId}")
    ResponseEntity<DataResult<ChatRoomDto>> getRoom(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    );

    @GetMapping("/rooms/{roomId}/messages")
    ResponseEntity<DataResult<List<ChatMessageDto>>> getMessages(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    );

    @GetMapping("/rooms/{roomId}/presence")
    ResponseEntity<DataResult<PresenceDto>> getRoomPresence(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping("/rooms/messages")
    ResponseEntity<DataResult<ChatMessageDto>> sendMessage(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody SendMessageRequest request
    );

    @PutMapping("/rooms/{roomId}/read")
    ResponseEntity<Result> markAsRead(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping("/rooms/{roomId}/accept")
    ResponseEntity<Result> acceptRequest(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping("/rooms/{roomId}/decline")
    ResponseEntity<Result> declineRequest(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    );

    @DeleteMapping("/rooms/{roomId}")
    ResponseEntity<Result> leaveRoom(
            @PathVariable Long roomId,
            @RequestAttribute("userId") Long currentUserId
    );

    @DeleteMapping("/messages/{messageId}")
    ResponseEntity<Result> deleteMessage(
            @PathVariable Long messageId,
            @RequestAttribute("userId") Long currentUserId
    );
}
