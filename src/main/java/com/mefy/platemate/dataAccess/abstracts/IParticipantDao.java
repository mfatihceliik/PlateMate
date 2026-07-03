package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IParticipantDao extends JpaRepository<Participant, Long> {
    // Rooms the user is participating in and hasn't hidden (deleted) from their own list.
    List<Participant> findByUserIdAndHiddenAtIsNull(Long userId);

    boolean existsByUserIdAndChatRoomId(Long userId, Long chatRoomId); // Check membership of the room

    Optional<Participant> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    @Query("SELECT p1.chatRoom FROM Participant p1 JOIN Participant p2 ON p1.chatRoom.id = p2.chatRoom.id " +
            "WHERE p1.user.id = :u1 AND p2.user.id = :u2 AND p1.chatRoom.isGroup = false")
    Optional<ChatRoom> findPrivateChatBetweenUsers(Long u1, Long u2);

    // Distinct users that share a 1-1 chat with the given user (presence partners)
    @Query("SELECT DISTINCT p2.user.id FROM Participant p1 JOIN Participant p2 ON p1.chatRoom.id = p2.chatRoom.id " +
            "WHERE p1.user.id = :userId AND p2.user.id <> :userId AND p1.chatRoom.isGroup = false")
    List<Long> findPrivateChatPartnerIds(Long userId);

    // The other participant ids of a room relative to the given user
    @Query("SELECT p.user.id FROM Participant p WHERE p.chatRoom.id = :roomId AND p.user.id <> :userId")
    List<Long> findOtherParticipantIds(Long roomId, Long userId);
}
