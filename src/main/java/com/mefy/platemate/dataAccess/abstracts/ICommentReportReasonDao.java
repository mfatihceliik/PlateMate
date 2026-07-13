package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.CommentReportReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICommentReportReasonDao extends JpaRepository<CommentReportReason, Long> {
    List<CommentReportReason> findByActiveTrueOrderBySortOrderAsc();

    List<CommentReportReason> findAllByOrderBySortOrderAsc();

    boolean existsByCode(String code);

    Optional<CommentReportReason> findByCode(String code);
}
