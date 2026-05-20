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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plate_search_events", indexes = {
        @Index(name = "idx_plate_search_events_searched_at", columnList = "searched_at"),
        @Index(name = "idx_plate_search_events_plate_id_searched_at", columnList = "plate_id,searched_at"),
        @Index(name = "idx_plate_search_events_user_id_searched_at", columnList = "user_id,searched_at"),
        @Index(name = "idx_plate_search_events_plate_id_user_id_searched_at", columnList = "plate_id,user_id,searched_at")
})
@Getter
@Setter
@NoArgsConstructor
public class PlateSearchEvent implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id", nullable = false)
    private Plate plate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
