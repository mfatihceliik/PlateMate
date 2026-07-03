package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_follows_follower_following",
                        columnNames = {"follower_id", "following_id"})
        },
        indexes = {
                @Index(name = "idx_follows_follower_id", columnList = "follower_id"),
                @Index(name = "idx_follows_following_id", columnList = "following_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class Follow implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    private void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
