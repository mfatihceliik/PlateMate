package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile implements IEntity {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String bio;
    private Double driverRating = 0.0; // Ortalama (Görüntüleme için)
    private Integer reviewCount = 0;   // Kaç kişi yorum yaptı?
    private Long totalRatingSum = 0L;  // Tüm puanların toplamı

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // UserProfile.java içine eklenecek kısım:
    @OneToMany(mappedBy = "targetProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserReview> reviews;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SocialMediaLink> socialMediaLinks;
}