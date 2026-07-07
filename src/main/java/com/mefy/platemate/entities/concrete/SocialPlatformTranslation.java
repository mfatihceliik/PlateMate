package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_platform_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"social_platform_id", "locale"})
})
@Getter
@Setter
@NoArgsConstructor
public class SocialPlatformTranslation implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_platform_id", nullable = false)
    private SocialPlatformLookup socialPlatform;

    @Column(name = "social_platform_id", insertable = false, updatable = false)
    private Long socialPlatformId;

    @Column(nullable = false, length = 5)
    private String locale;

    @Column(nullable = false, length = 128)
    private String label;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
