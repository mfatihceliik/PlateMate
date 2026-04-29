import com.mefy.platemate.business.abstracts.IChatMessageService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.ChatMessageMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IChatMessageDao;
import com.mefy.platemate.dataAccess.abstracts.IChatRoomDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSettingsDao;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.dto.ChatMessageDto;
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
    private final IUserSettingsDao userSettingsDao;
    private final INotificationService notificationService;
    private final ChatMessageMapper chatMessageMapper;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<ChatMessageDto> sendMessage(ChatMessage message) {
        ChatRoom room = chatRoomDao.findById(message.getChatRoom().getId()).orElseThrow();
        
        // Karşı tarafın mesajlaşma iznini kontrol et (Business Rule)
        if (!room.isGroup()) {
            room.getParticipants().stream()
                    .filter(p -> !p.getUser().getId().equals(message.getSender().getId()))
                    .findFirst()
                    .ifPresent(recipient -> {
                        var settings = userSettingsDao.findByUserId(recipient.getUser().getId()).orElse(null);
                        if (settings != null && !settings.isMessagingEnabled()) {
                            throw new RuntimeException(messageService.getMessage(Messages.MESSAGING_DISABLED));
                        }
                    });
        }

        chatMessageDao.save(message);

        room.setLastMessageAt(LocalDateTime.now());
        chatRoomDao.save(room);

        // Bildirim gönder (Alıcıya)
        if (!room.isGroup()) {
            room.getParticipants().stream()
                    .filter(p -> !p.getUser().getId().equals(message.getSender().getId()))
                    .findFirst()
                    .ifPresent(recipient -> {
                        String title = messageService.getMessage(Messages.NOTIFICATION_NEW_MESSAGE_TITLE);
                        String content = message.getSender().getUsername() + " " + messageService.getMessage(Messages.NOTIFICATION_NEW_MESSAGE_CONTENT);
                        notificationService.sendNotification(recipient.getUser().getId(), title, content, NotificationType.MESSAGE);
                    });
        }

        ChatMessageDto dto = chatMessageMapper.entityToDto(message);
        return new SuccessDataResult<>(dto, messageService.getMessage(Messages.MESSAGE_SENT));
    }

    @Override
    public DataResult<List<ChatMessageDto>> getMessagesByRoomId(Long roomId) {
        List<ChatMessage> messages = chatMessageDao.findByChatRoomIdOrderBySentAtAsc(roomId);
        List<ChatMessageDto> dtos = messages.stream()
                .map(chatMessageMapper::entityToDto)
                .collect(Collectors.toList());

        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.MESSAGES_LISTED));
    }

    @Override
    @Transactional
    public Result markAsRead(Long roomId, Long userId) {
        List<ChatMessage> unreadMessages = chatMessageDao.findByChatRoomIdOrderBySentAtAsc(roomId);

        unreadMessages.stream()
                .filter(m -> !m.getSender().getId().equals(userId) && !m.isRead())
                .forEach(m -> m.setRead(true));

        chatMessageDao.saveAll(unreadMessages);
        return new SuccessResult(messageService.getMessage("messages.read"));
    }
}
