package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.ChatMessageMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IChatMessageDao;
import com.mefy.platemate.dataAccess.abstracts.IChatRoomDao;
import com.mefy.platemate.dataAccess.abstracts.IParticipantDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageManager implements IChatMessageService {

    private final IChatMessageDao chatMessageDao;
    private final IChatRoomDao chatRoomDao;
    private final IParticipantDao participantDao;
    private final IUserSettingsDao userSettingsDao;
    private final INotificationService notificationService;
    private final ChatMessageMapper chatMessageMapper;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<ChatMessageDto> sendMessage(ChatMessage message, Long currentUserId) {
        ChatRoom room = findRoom(resolveRoomId(message));
        Result senderResult = ensureMessageSenderMatchesCurrentUser(message, currentUserId);
        if (!senderResult.isSuccess()) {
            return new ErrorDataResult<>(senderResult.getMessage());
        }
        return processSendMessage(message, room, currentUserId);
    }

    @Override
    @Transactional
    public DataResult<ChatMessageDto> sendMessage(SendMessageRequest request, Long currentUserId) {
        ChatRoom room = findRoom(request.getChatRoomId());

        User sender = new User();
        sender.setId(currentUserId);

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setChatRoom(room);
        message.setContent(request.getContent());

        return processSendMessage(message, room, currentUserId);
    }

    private DataResult<ChatMessageDto> processSendMessage(ChatMessage message, ChatRoom room, Long currentUserId) {
        Result validationResult = validateSendMessage(room, currentUserId);
        if (validationResult != null) {
            return new ErrorDataResult<>(validationResult.getMessage());
        }

        persistMessageAndTouchRoom(message, room);
        notifyRecipientIfNeeded(room, message, currentUserId);

        ChatMessageDto dto = chatMessageMapper.entityToDto(message);
        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.MESSAGE_SENT));
    }

    private Result validateSendMessage(ChatRoom room, Long currentUserId) {
        return BusinessRules.run(
                ensureUserIsRoomParticipant(room, currentUserId),
                room != null && !room.isGroup() ? ensureRecipientMessagingEnabled(room, currentUserId) : new SuccessResult()
        );
    }

    private void persistMessageAndTouchRoom(ChatMessage message, ChatRoom room) {
        chatMessageDao.save(message);
        if (room != null) {
            room.setLastMessageAt(LocalDateTime.now());
            chatRoomDao.save(room);
        }
    }

    private void notifyRecipientIfNeeded(ChatRoom room, ChatMessage message, Long currentUserId) {
        if (room == null || room.isGroup()) {
            return;
        }

        findRecipient(room, currentUserId).ifPresent(recipient -> {
            String title = messageService.getMessage(Messages.NOTIFICATION_NEW_MESSAGE_TITLE);
            String senderUsername = findSenderUsername(room, message, currentUserId);
            String content = buildMessageNotificationContent(senderUsername);
            notificationService.sendNotification(recipient.getUser().getId(), title, content, NotificationType.MESSAGE);
        });
    }

    private java.util.Optional<com.mefy.platemate.entities.concrete.Participant> findRecipient(ChatRoom room, Long currentUserId) {
        return room.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .findFirst();
    }

    private String findSenderUsername(ChatRoom room, ChatMessage message, Long currentUserId) {
        if (message.getSender() != null && message.getSender().getUsername() != null && !message.getSender().getUsername().isBlank()) {
            return message.getSender().getUsername();
        }
        return room.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .map(p -> p.getUser().getUsername())
                .orElse("");
    }

    private String buildMessageNotificationContent(String senderUsername) {
        return senderUsername + " " + messageService.getMessage(Messages.NOTIFICATION_NEW_MESSAGE_CONTENT);
    }

    @Override
    public DataResult<List<ChatMessageDto>> getMessagesByRoomId(Long roomId, Long currentUserId) {
        ChatRoom room = findRoom(roomId);
        Result accessResult = ensureUserIsRoomParticipant(room, currentUserId);
        if (!accessResult.isSuccess()) {
            return new ErrorDataResult<>(accessResult.getMessage());
        }

        List<ChatMessage> messages = chatMessageDao.findByChatRoomIdOrderBySentAtAsc(roomId);
        List<ChatMessageDto> dtos = messages.stream()
                .map(chatMessageMapper::entityToDto)
                .collect(Collectors.toList());

        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.MESSAGES_LISTED));
    }

    @Override
    @Transactional
    public Result markAsRead(Long roomId, Long currentUserId) {
        ChatRoom room = findRoom(roomId);
        Result accessResult = ensureUserIsRoomParticipant(room, currentUserId);
        if (!accessResult.isSuccess()) {
            return accessResult;
        }

        List<ChatMessage> unreadMessages = chatMessageDao.findByChatRoomIdOrderBySentAtAsc(roomId);

        unreadMessages.stream()
                .filter(m -> !m.getSender().getId().equals(currentUserId) && !m.isRead())
                .forEach(m -> m.setRead(true));

        chatMessageDao.saveAll(unreadMessages);
        return new SuccessResult(messageService.getMessage("messages.read"));
    }

    private Long resolveRoomId(ChatMessage message) {
        if (message == null || message.getChatRoom() == null) {
            return null;
        }
        return message.getChatRoom().getId();
    }

    private ChatRoom findRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }
        return chatRoomDao.findById(roomId).orElse(null);
    }

    private Result ensureUserIsRoomParticipant(ChatRoom room, Long currentUserId) {
        if (room == null || room.getId() == null || currentUserId == null || !isUserParticipantOfRoom(currentUserId, room.getId())) {
            return new ErrorResult(messageService.getMessage("auth.unauthorized"));
        }
        return new SuccessResult();
    }

    private boolean isUserParticipantOfRoom(Long userId, Long roomId) {
        return participantDao.existsByUserIdAndChatRoomId(userId, roomId);
    }

    private Result ensureMessageSenderMatchesCurrentUser(ChatMessage message, Long currentUserId) {
        if (message == null
                || message.getSender() == null
                || message.getSender().getId() == null
                || !message.getSender().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage("auth.unauthorized"));
        }
        return new SuccessResult();
    }

    private Result ensureRecipientMessagingEnabled(ChatRoom room, Long senderId) {
        return room.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(senderId))
                .findFirst()
                .map(recipient -> {
                    var settings = userSettingsDao.findByUserId(recipient.getUser().getId()).orElse(null);
                    if (settings != null && !settings.isMessagingEnabled()) {
                        return new ErrorResult(messageService.getMessage(Messages.MESSAGING_DISABLED));
                    }
                    return new SuccessResult();
                })
                .orElse(new SuccessResult());
    }
}
