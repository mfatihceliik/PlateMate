package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reviewer_id", "target_profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserReview implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating; // 1-5 arası puan

    @Column(nullable = false, length = 1000)
    private String comment; // Kullanıcının yazdığı yorum

    private LocalDateTime createdAt = LocalDateTime.now();

    // Yorumu yazan kişi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    // Hakkında yorum yapılan profil
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_profile_id", nullable = false)
    private UserProfile targetProfile;
}