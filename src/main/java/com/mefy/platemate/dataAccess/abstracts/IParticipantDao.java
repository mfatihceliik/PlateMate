package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.ChatRoom;
import com.mefy.platemate.entities.concrete.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IParticipantDao extends JpaRepository<Participant, Long> {
    List<Participant> findByUserId(Long userId); // Kullanıcının dahil olduğu odalar

    @Query("SELECT p1.chatRoom FROM Participant p1 JOIN Participant p2 ON p1.chatRoom.id = p2.chatRoom.id " +
            "WHERE p1.user.id = :u1 AND p2.user.id = :u2 AND p1.chatRoom.isGroup = false")
    Optional<ChatRoom> findPrivateChatBetweenUsers(Long u1, Long u2);
}
