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

    private boolean messagingEnabled = true;      // Enable/disable messaging
    private boolean onlineVisibilityEnabled = true;  // Show online status to others (reciprocal)
    private boolean messageNotificationsEnabled = true;    // Message notifications
    private boolean friendNotificationsEnabled = true;      // Friendship notifications
    private boolean plateReviewNotificationsEnabled = true;  // Followed plate review notifications
    private boolean newFollowerNotificationsEnabled = true;   // New follower notifications
    private boolean reviewReplyNotificationsEnabled = true;   // Review reply notifications
}
