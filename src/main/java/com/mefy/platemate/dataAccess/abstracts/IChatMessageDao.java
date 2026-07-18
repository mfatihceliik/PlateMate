package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IChatMessageDao extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    // Bir odanın mesajlarını (silinenler haric) getirir: taze bir senkron, o cihazda hic
    // gorulmemis eski silinmis mesajlari yerel onbellege tekrar getirmesin diye.
    List<ChatMessage> findByChatRoomIdAndStatusNotOrderBySentAtAsc(Long chatRoomId, MessageStatus status);

    long countByChatRoomIdAndSenderId(Long chatRoomId, Long senderId);

    // Idempotency guard: lets a retried send recognize "this already committed, the ack was
    // just lost" instead of persisting a duplicate row.
    Optional<ChatMessage> findFirstBySenderIdAndClientMessageId(Long senderId, String clientMessageId);

    long countByChatRoomIdAndSenderIdNotAndStatusNot(Long chatRoomId, Long senderId, MessageStatus status);

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.status = :status
              AND m.sender.id <> :userId
              AND m.chatRoom.id IN (
                  SELECT p.chatRoom.id FROM Participant p WHERE p.user.id = :userId
              )
            """)
    List<ChatMessage> findIncomingByStatusForUser(@Param("userId") Long userId,
                                                   @Param("status") MessageStatus status);
}
