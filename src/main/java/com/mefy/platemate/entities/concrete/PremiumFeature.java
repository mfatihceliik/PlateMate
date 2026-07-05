package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "premium_features")
@Getter
@Setter
@NoArgsConstructor
public class PremiumFeature implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "icon_key", nullable = false, length = 48)
    private String iconKey;

    @Column(name = "title_tr", nullable = false, length = 160)
    private String titleTr;

    @Column(name = "title_en", nullable = false, length = 160)
    private String titleEn;

    @Column(name = "subtitle_tr", length = 300)
    private String subtitleTr;

    @Column(name = "subtitle_en", length = 300)
    private String subtitleEn;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
