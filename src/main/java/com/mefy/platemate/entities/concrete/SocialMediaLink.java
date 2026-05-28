package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@Entity
@Table(name = "social_media_links")
@Getter
@Setter
@NoArgsConstructor
public class SocialMediaLink implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform_id", nullable = false)
    private Long platformId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", insertable = false, updatable = false)
    private SocialPlatformLookup platformRef;

    private String url;

    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    @Transient
    public SocialPlatform getPlatform() {
        SocialPlatform fromId = SocialPlatform.fromId(platformId);
        if (fromId != null) {
            return fromId;
        }
        return SocialPlatform.fromCode(resolvePlatformCodeFromRef());
    }

    public void setPlatform(SocialPlatform platform) {
        this.platformId = platform == null ? null : platform.getId();
    }

    @Transient
    public String getPlatformCode() {
        SocialPlatform platform = SocialPlatform.fromId(platformId);
        if (platform != null) {
            return platform.getCode();
        }
        return resolvePlatformCodeFromRef();
    }

    private String resolvePlatformCodeFromRef() {
        if (platformRef == null || !Hibernate.isInitialized(platformRef)) {
            return null;
        }
        return platformRef.getCode();
    }
}
