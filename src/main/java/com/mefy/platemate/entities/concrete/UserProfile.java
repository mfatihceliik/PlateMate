package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "display_name", length = 50)
    private String displayName;

    @Column(length = 300)
    private String bio;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(nullable = false)
    private boolean verified = false;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SocialMediaLink> socialMediaLinks;
}
