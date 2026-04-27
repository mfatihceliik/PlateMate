package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IChatRoomDao extends JpaRepository<ChatRoom, Long> {
}
