package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.CommentReport;
import com.mefy.platemate.entities.concrete.CommentReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ICommentReportDao extends JpaRepository<CommentReport, Long> {
    boolean existsByCommentIdAndReporterUserId(Long commentId, Long reporterUserId);

    long countByCommentIdAndStatusIn(Long commentId, Collection<CommentReportStatus> statuses);

    Page<CommentReport> findByStatusOrderByCreatedAtAsc(CommentReportStatus status, Pageable pageable);

    Page<CommentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("""
            delete from CommentReport cr
            where cr.createdAt < :cutoff
              and cr.status in :resolvedStatuses
            """)
    int deleteResolvedByCreatedAtBefore(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("resolvedStatuses") List<CommentReportStatus> resolvedStatuses
    );
}
