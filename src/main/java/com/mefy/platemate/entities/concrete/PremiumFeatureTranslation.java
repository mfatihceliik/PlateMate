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
@Table(name = "premium_feature_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"premium_feature_id", "locale"})
})
@Getter
@Setter
@NoArgsConstructor
public class PremiumFeatureTranslation implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "premium_feature_id", nullable = false)
    private PremiumFeature premiumFeature;

    @Column(name = "premium_feature_id", insertable = false, updatable = false)
    private Long premiumFeatureId;

    @Column(nullable = false, length = 5)
    private String locale;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 300)
    private String subtitle;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
