package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
public class UserSettings implements IEntity {
    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private boolean messagingEnabled = true;      // Mesajlaşma aç/kapa
    private boolean locationSharingEnabled = true; // Lokasyon paylaşımı aç/kapa
    private boolean notificationsEnabled = true;   // Bildirim aç/kapa (ileride)
}
