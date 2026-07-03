package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.abstracts.ISocketPushService;
import com.mefy.platemate.business.abstracts.IUserBlockService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.constants.SocketEvents;
import com.mefy.platemate.core.utilities.mappers.ChatMessageMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IChatMessageDao;
import com.mefy.platemate.dataAccess.abstracts.IChatRoomDao;
import com.mefy.platemate.dataAccess.abstracts.IParticipantDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.ChatRoomRequestStatus;
import com.mefy.platemate.entities.concrete.MessageStatus;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.concrete.Participant;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.ChatMessageDto;
import com.mefy.platemate.entities.dto.MessageStatusSignalDto;
import com.mefy.platemate.entities.dto.request.SendMessageRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageManager implements IChatMessageService {

    private final IChatMessageDao chatMessageDao;
    private final IChatRoomDao chatRoomDao;
    private final IParticipantDao participantDao;
    private final IUserSettingsDao userSettingsDao;
    private final INotificationService notificationService;
    private final ISocketPushService socketPushService;
    private final IUserBlockService userBlockService;
    private final ChatMessageMapper chatMessageMapper;
    private final IAppSettingsService appSettingsService;
    private final IMessageService messageService;


    @Override
    @Transactional
    public DataResult<ChatMessageDto> sendMessage(SendMessageRequest request, Long currentUserId) {
        ChatRoom room = findRoom(request.getChatRoomId());

        Result validationResult = validateSendMessage(room, currentUserId);
        if (validationResult != null) {
            return new ErrorDataResult<>(validationResult.getMessage());
        }

        User sender = new User();
        sender.setId(currentUserId);

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setChatRoom(room);
        message.setContent(request.getContent());
        message.setStatus(MessageStatus.SENT);

        persistMessageAndTouchRoom(message, room);
        acceptRequestIfApproverReplied(room, currentUserId);
        unhideRecipientIfHidden(room, currentUserId);

        ChatMessageDto dto = chatMessageMapper.entityToDto(message);

        // Side-effects (live broadcast + delivery push + notifications) must never roll back the
        // persisted message. Isolate them so a socket/FCM failure cannot undo the @Transactional save.
        dispatchSendSideEffects(room, message, dto, currentUserId);

        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.MESSAGE_SENT));
    }

    private Result validateSendMessage(ChatRoom room, Long currentUserId) {
        Result participantResult = ensureUserIsRoomParticipant(room, currentUserId);
        if (!participantResult.isSuccess()) {
            return participantResult;
        }
        if (room.isGroup()) {
            return null;
        }

        Long recipientId = getOtherParticipantId(room, currentUserId);

        if (userBlockService.isBlockedEitherWay(currentUserId, recipientId)) {
            return new ErrorResult(messageService.getMessage(Messages.MESSAGING_BLOCKED));
        }

        Result messagingEnabledResult = ensureRecipientMessagingEnabled(room, currentUserId);
        if (!messagingEnabledResult.isSuccess()) {
            return messagingEnabledResult;
        }

        return validateRequestGating(room, currentUserId);
    }

    private Result validateRequestGating(ChatRoom room, Long currentUserId) {
        ChatRoomRequestStatus status = room.getRequestStatus();
        boolean isInitiator = currentUserId.equals(room.getInitiatorId());

        if (status == ChatRoomRequestStatus.PENDING && isInitiator) {
            long sentByInitiator = chatMessageDao.countByChatRoomIdAndSenderId(room.getId(), currentUserId);
            int preApprovalMessageLimit = appSettingsService.getInt(AppSettingKey.PRE_APPROVAL_MESSAGE_LIMIT);
            if (sentByInitiator >= preApprovalMessageLimit) {
                return new ErrorResult(messageService.getMessage(Messages.MESSAGE_REQUEST_LIMIT_REACHED));
            }
        }

        if (status == ChatRoomRequestStatus.DECLINED && isInitiator) {
            return new ErrorResult(messageService.getMessage(Messages.MESSAGE_REQUEST_DECLINED));
        }

        return null;
    }

    // Replying to a request (by the user who did NOT start it) implicitly accepts it.
    private void acceptRequestIfApproverReplied(ChatRoom room, Long currentUserId) {
        if (room == null || room.isGroup()) {
            return;
        }
        boolean isApprover = room.getInitiatorId() != null && !currentUserId.equals(room.getInitiatorId());
        if (isApprover && room.getRequestStatus() != ChatRoomRequestStatus.ACCEPTED) {
            room.setRequestStatus(ChatRoomRequestStatus.ACCEPTED);
            chatRoomDao.save(room);
        }
    }

    // Best-effort, exception-isolated post-persist work. Logged but swallowed so the message
    // row always commits even if delivery/notification dispatch fails.
    private void dispatchSendSideEffects(ChatRoom room, ChatMessage message, ChatMessageDto dto, Long currentUserId) {
        try {
            broadcastNewMessage(room, dto);
        } catch (Exception e) {
            log.error("Failed to broadcast message {}: {}", message.getId(), e.getMessage());
        }
        try {
            markDeliveredIfRecipientOnline(room, message, currentUserId);
        } catch (Exception e) {
            log.error("Failed to mark message {} delivered: {}", message.getId(), e.getMessage());
        }
        try {
            notifyRecipientIfNeeded(room, message, currentUserId);
        } catch (Exception e) {
            log.error("Failed to notify recipient for message {}: {}", message.getId(), e.getMessage());
        }
    }

    // Push the new message to every participant's own user room (joined on connect + every
    // reconnect), the sender included so they receive their own echo. Targeting user rooms — not
    // the chat room — removes the join_room race and reaches members who never joined the room.
    private void broadcastNewMessage(ChatRoom room, ChatMessageDto dto) {
        if (room == null || room.getParticipants() == null) {
            return;
        }
        room.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .distinct()
                .forEach(userId -> socketPushService.sendToUser(userId, SocketEvents.NEW_MESSAGE, dto));
    }

    private void markDeliveredIfRecipientOnline(ChatRoom room, ChatMessage message, Long currentUserId) {
        if (room == null || room.isGroup()) {
            return;
        }
        Long recipientId = getOtherParticipantId(room, currentUserId);
        if (recipientId == null || !socketPushService.isUserOnline(recipientId)) {
            return;
        }
        message.setStatus(MessageStatus.DELIVERED);
        message.setDeliveredAt(LocalDateTime.now());
        chatMessageDao.save(message);

        // Tell the sender their message reached the recipient (double tick).
        socketPushService.sendToUser(
                currentUserId,
                SocketEvents.MESSAGE_DELIVERED,
                new MessageStatusSignalDto(room.getId(), message.getId(), MessageStatus.DELIVERED.name())
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
            String senderUsername = findSenderUsername(room, message, currentUserId);
            String content = message.getContent();
            notificationService.sendNotification(
                    recipient.getUser().getId(), senderUsername, content,
                    NotificationType.MESSAGE.name(), room.getId());
        });
    }

    // Sending a message revives a conversation the recipient had self-hidden ("deleted"),
    // mirroring the WhatsApp-style "it comes back when they message you" behavior.
    private void unhideRecipientIfHidden(ChatRoom room, Long currentUserId) {
        findRecipient(room, currentUserId)
                .filter(p -> p.getHiddenAt() != null)
                .ifPresent(p -> {
                    p.setHiddenAt(null);
                    participantDao.save(p);
                });
    }

    private Long getOtherParticipantId(ChatRoom room, Long currentUserId) {
        return findRecipient(room, currentUserId)
                .map(p -> p.getUser().getId())
                .orElse(null);
    }

    private Optional<Participant> findRecipient(ChatRoom room, Long currentUserId) {
        if (room == null || room.getParticipants() == null) {
            return Optional.empty();
        }
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

        List<ChatMessage> messages = chatMessageDao.findByChatRoomIdOrderBySentAtAsc(roomId);

        List<ChatMessage> newlyRead = messages.stream()
                .filter(m -> !m.getSender().getId().equals(currentUserId) && m.getStatus() != MessageStatus.READ)
                .collect(Collectors.toList());

        if (newlyRead.isEmpty()) {
            return new SuccessResult(messageService.getMessage(Messages.MESSAGES_READ));
        }

        LocalDateTime now = LocalDateTime.now();
        newlyRead.forEach(m -> {
            m.setStatus(MessageStatus.READ);
            m.setRead(true);
            if (m.getReadAt() == null) m.setReadAt(now);
        });
        chatMessageDao.saveAll(newlyRead);

        // Notify each sender that their messages in this room were read (tick color change).
        newlyRead.stream()
                .map(m -> m.getSender().getId())
                .distinct()
                .forEach(senderId -> socketPushService.sendToUser(
                        senderId,
                        SocketEvents.MESSAGE_READ,
                        new MessageStatusSignalDto(roomId, null, MessageStatus.READ.name())
                ));

        return new SuccessResult(messageService.getMessage(Messages.MESSAGES_READ));
    }

    @Override
    @Transactional
    public void markPendingDelivered(Long userId) {
        List<ChatMessage> pending = chatMessageDao.findIncomingByStatusForUser(userId, MessageStatus.SENT);
        if (pending.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        pending.forEach(m -> {
            m.setStatus(MessageStatus.DELIVERED);
            if (m.getDeliveredAt() == null) m.setDeliveredAt(now);
        });
        chatMessageDao.saveAll(pending);

        // Notify each sender their message has now been delivered (double tick).
        pending.forEach(m -> socketPushService.sendToUser(
                m.getSender().getId(),
                SocketEvents.MESSAGE_DELIVERED,
                new MessageStatusSignalDto(m.getChatRoom().getId(), m.getId(), MessageStatus.DELIVERED.name())
        ));
    }


    private ChatRoom findRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }
        return chatRoomDao.findById(roomId).orElse(null);
    }

    private Result ensureUserIsRoomParticipant(ChatRoom room, Long currentUserId) {
        if (room == null || room.getId() == null || currentUserId == null || !isUserParticipantOfRoom(currentUserId, room.getId())) {
            return new ErrorResult(messageService.getMessage(Messages.AUTH_UNAUTHORIZED));
        }
        return new SuccessResult();
    }

    private boolean isUserParticipantOfRoom(Long userId, Long roomId) {
        return participantDao.existsByUserIdAndChatRoomId(userId, roomId);
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
