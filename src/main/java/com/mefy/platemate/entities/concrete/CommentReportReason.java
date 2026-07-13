package com.mefy.platemate.entities.concrete;

import com.mefy.platemate.entities.abstracts.IEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Admin-managed catalog of comment report reasons (previously a fixed Java enum).
 * The 7 built-in codes keep their V11 ids; the public label is resolved with an
 * {@code enum.comment_report_reason.<CODE>} i18n key falling back to this entity's label.
 */
@Entity
@Table(name = "comment_report_reasons", indexes = {
        @Index(name = "idx_comment_report_reasons_active_sort", columnList = "active,sort_order")
})
@Getter
@Setter
@NoArgsConstructor
public class CommentReportReason implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String label;

    @Column(name = "requires_description", nullable = false)
    private boolean requiresDescription;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
