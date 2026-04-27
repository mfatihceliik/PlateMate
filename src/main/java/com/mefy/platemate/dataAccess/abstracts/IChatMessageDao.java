package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IChatMessageDao extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
}
