package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
public class Friendship implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sent the friendship request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // The user who received the friendship request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Column(name = "status_id", nullable = false)
    private Long statusId = FriendshipRequestStatusCodes.REQUESTED_ID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private FriendshipRequestStatusLookup statusRef;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime respondedAt; // Accepted/Rejected date

    @Transient
    public String getStatusCode() {
        String codeFromId = FriendshipRequestStatusCodes.codeFromId(statusId);
        if (codeFromId != null) {
            return codeFromId;
        }
        if (statusRef == null || !Hibernate.isInitialized(statusRef)) {
            return null;
        }
        return statusRef.getCode();
    }
}
