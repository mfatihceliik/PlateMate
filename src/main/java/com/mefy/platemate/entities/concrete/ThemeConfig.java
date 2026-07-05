package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Single-row (id = 1) config for the accent-color picker grid column count. */
@Entity
@Table(name = "theme_config")
@Getter
@Setter
@NoArgsConstructor
public class ThemeConfig implements IEntity {
    @Id
    private Long id;

    @Column(name = "grid_size", nullable = false)
    private Integer gridSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
