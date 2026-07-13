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

    long countByCommentIdAndStatusIdIn(Long commentId, Collection<Long> statusIds);

    Page<CommentReport> findByStatusIdOrderByCreatedAtAsc(Long statusId, Pageable pageable);

    Page<CommentReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatusId(Long statusId);

    @Modifying
    @Query("""
            delete from CommentReport cr
            where cr.createdAt < :cutoff
              and cr.statusId in :resolvedStatusIds
            """)
    int deleteResolvedByCreatedAtBeforeAndStatusIdIn(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("resolvedStatusIds") List<Long> resolvedStatusIds
    );

    default long countByCommentIdAndStatusIn(Long commentId, Collection<CommentReportStatus> statuses) {
        List<Long> ids = statuses == null ? List.of() : statuses.stream().map(CommentReportStatus::getId).toList();
        return countByCommentIdAndStatusIdIn(commentId, ids);
    }

    default Page<CommentReport> findByStatusOrderByCreatedAtAsc(CommentReportStatus status, Pageable pageable) {
        return findByStatusIdOrderByCreatedAtAsc(status == null ? null : status.getId(), pageable);
    }

    default int deleteResolvedByCreatedAtBefore(LocalDateTime cutoff, List<CommentReportStatus> resolvedStatuses) {
        List<Long> ids = resolvedStatuses == null ? List.of() : resolvedStatuses.stream().map(CommentReportStatus::getId).toList();
        return deleteResolvedByCreatedAtBeforeAndStatusIdIn(cutoff, ids);
    }
}
