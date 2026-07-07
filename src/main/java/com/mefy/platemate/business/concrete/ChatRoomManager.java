package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IChatRoomService;
import com.mefy.platemate.business.abstracts.IFriendshipService;
import com.mefy.platemate.business.abstracts.IUserBlockService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.ChatRoomMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IChatMessageDao;
import com.mefy.platemate.dataAccess.abstracts.IChatRoomDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.business.abstracts.IParticipantService;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.ChatRoomRequestStatus;
import com.mefy.platemate.entities.concrete.MessageStatus;
import com.mefy.platemate.entities.concrete.Participant;
import com.mefy.platemate.entities.dto.ChatRoomDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomManager implements IChatRoomService {

    private final IChatRoomDao chatRoomDao;
    private final IChatMessageDao chatMessageDao;
    private final IParticipantService participantService;
    private final IUserSettingsDao userSettingsDao;
    private final IFriendshipService friendshipService;
    private final IUserBlockService userBlockService;
    private final ChatRoomMapper chatRoomMapper;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<ChatRoomDto> getOrCreateChatRoom(Long userOneId, Long userTwoId) {
        // Blocked users (either direction) cannot start a conversation
        if (userBlockService.isBlockedEitherWay(userOneId, userTwoId)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.MESSAGING_BLOCKED));
        }

        // Check target user's messaging setting
        var targetSettings = userSettingsDao.findByUserId(userTwoId).orElse(null);
        if (targetSettings != null && !targetSettings.isMessagingEnabled()) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.MESSAGING_DISABLED));
        }

        ChatRoom existingRoom = findPrivateChatBetweenUsers(userOneId, userTwoId);

        if (existingRoom != null) {
            // Starting a conversation with someone whose room this user had self-hidden
            // ("deleted") should bring it back into their own list too.
            participantService.unhideForUser(existingRoom.getId(), userOneId);
            return new SuccessDataResult<>(toDtoWithUnread(existingRoom, userOneId), messageService.getMessage(Messages.CHAT_ROOM_FOUND));
        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setGroup(false);
        newRoom.setCreatedAt(LocalDateTime.now());

        // Friends can message freely; strangers need approval (message request)
        if (friendshipService.areFriends(userOneId, userTwoId)) {
            newRoom.setRequestStatus(ChatRoomRequestStatus.ACCEPTED);
        } else {
            newRoom.setRequestStatus(ChatRoomRequestStatus.PENDING);
            newRoom.setInitiatorId(userOneId);
        }
        chatRoomDao.save(newRoom);

        addParticipantToRoom(newRoom, userOneId);
        addParticipantToRoom(newRoom, userTwoId);

        return new SuccessDataResult<>(toDtoWithUnread(newRoom, userOneId), messageService.getMessage(Messages.CHAT_ROOM_CREATED));
    }

    @Override
    public DataResult<ChatRoomDto> getRoom(Long roomId, Long currentUserId) {
        ChatRoom room = roomId == null ? null : chatRoomDao.findById(roomId).orElse(null);
        if (room == null || !participantService.isRoomMember(currentUserId, roomId)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.AUTH_UNAUTHORIZED));
        }
        return new SuccessDataResult<>(toDtoWithUnread(room, currentUserId), messageService.getMessage(Messages.CHAT_ROOM_FOUND));
    }

    @Override
    @Transactional
    public Result leaveRoom(Long roomId, Long currentUserId) {
        if (roomId == null || !participantService.isRoomMember(currentUserId, roomId)) {
            return new ErrorResult(messageService.getMessage(Messages.AUTH_UNAUTHORIZED));
        }
        participantService.removeFromRoom(roomId, currentUserId);
        return new SuccessResult(messageService.getMessage(Messages.CHAT_ROOM_LEFT));
    }

    @Override
    @Transactional
    public Result respondToRequest(Long roomId, Long currentUserId, boolean accept) {
        ChatRoom room = roomId == null ? null : chatRoomDao.findById(roomId).orElse(null);
        if (room == null || !participantService.isRoomMember(currentUserId, roomId)) {
            return new ErrorResult(messageService.getMessage(Messages.AUTH_UNAUTHORIZED));
        }
        // Only the approver (the user who did NOT start the request) may respond
        if (currentUserId.equals(room.getInitiatorId())) {
            return new ErrorResult(messageService.getMessage(Messages.MESSAGE_REQUEST_RESPOND_UNAUTHORIZED));
        }

        room.setRequestStatus(accept ? ChatRoomRequestStatus.ACCEPTED : ChatRoomRequestStatus.DECLINED);
        chatRoomDao.save(room);

        return new SuccessResult(messageService.getMessage(
                accept ? Messages.MESSAGE_REQUEST_ACCEPTED : Messages.MESSAGE_REQUEST_DECLINED_OK));
    }

    @Override
    public DataResult<List<ChatRoomDto>> getUserRooms(Long userId) {
        List<Long> roomIds = participantService.getByUserId(userId).getData();
        List<ChatRoom> chatRooms = chatRoomDao.findAllById(roomIds);
        
        List<ChatRoomDto> dtos = chatRooms.stream()
                .map(room -> toDtoWithUnread(room, userId))
                .collect(Collectors.toList());

        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.USER_ROOMS_LISTED));
    }

    // Maps a room to a DTO and stamps the current user's unread count (messages not sent by
    // them and not yet READ). Centralized so every list/detail response carries it consistently.
    private ChatRoomDto toDtoWithUnread(ChatRoom room, Long currentUserId) {
        ChatRoomDto dto = chatRoomMapper.entityToDto(room, currentUserId);
        if (dto != null) {
            dto.setUnreadCount((int) chatMessageDao
                    .countByChatRoomIdAndSenderIdNotAndStatusNot(room.getId(), currentUserId, MessageStatus.READ));
        }
        return dto;
    }

    private void addParticipantToRoom(ChatRoom room, Long userId) {
        participantService.addParticipantToRoom(room.getId(), userId);
    }

    private ChatRoom findPrivateChatBetweenUsers(Long id1, Long id2) {
        Long roomId = participantService.findPrivateChatBetweenUsers(id1, id2).getData();
        if (roomId == null) {
            return null;
        }
        return chatRoomDao.findById(roomId).orElse(null);
    }
}

