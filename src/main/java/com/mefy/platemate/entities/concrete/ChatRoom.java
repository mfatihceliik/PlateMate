package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName; // Can be null for 1-1 chats, populated for groups

    private boolean isGroup = false; // Distinguishes between 1-1 and group chats

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 20)
    private ChatRoomRequestStatus requestStatus = ChatRoomRequestStatus.ACCEPTED;

    // For 1-1 message requests: the user who started the conversation (must wait for approval)
    @Column(name = "initiator_id")
    private Long initiatorId;

    private LocalDateTime createdAt = LocalDateTime.now();


    private LocalDateTime lastMessageAt;

    // Ordered so the preview mapper's messages.get(size-1) is deterministically the latest message.
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<ChatMessage> messages;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<Participant> participants;
}
