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

    private String roomName; // 1-1 mesajlarda boş kalabilir, gruplarda isim alır

    private boolean isGroup = false; // 1-1 mi yoksa grup mu ayrımı için

    private LocalDateTime createdAt = LocalDateTime.now();


    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<Participant> participants;
}
