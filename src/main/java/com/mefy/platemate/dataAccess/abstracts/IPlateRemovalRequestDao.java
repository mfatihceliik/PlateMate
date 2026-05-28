package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateRemovalRequest;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IPlateRemovalRequestDao extends JpaRepository<PlateRemovalRequest, Long> {
    Page<PlateRemovalRequest> findByStatusIdOrderByCreatedAtAsc(Long statusId, Pageable pageable);

    Page<PlateRemovalRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("""
            delete from PlateRemovalRequest pr
            where pr.createdAt < :cutoff
              and pr.statusId in :resolvedStatusIds
            """)
    int deleteResolvedByCreatedAtBeforeAndStatusIdIn(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("resolvedStatusIds") List<Long> resolvedStatusIds
    );

    default Page<PlateRemovalRequest> findByStatusOrderByCreatedAtAsc(PlateRemovalRequestStatus status, Pageable pageable) {
        return findByStatusIdOrderByCreatedAtAsc(status == null ? null : status.getId(), pageable);
    }

    default int deleteResolvedByCreatedAtBefore(LocalDateTime cutoff, List<PlateRemovalRequestStatus> resolvedStatuses) {
        List<Long> ids = resolvedStatuses == null ? List.of() : resolvedStatuses.stream().map(PlateRemovalRequestStatus::getId).toList();
        return deleteResolvedByCreatedAtBeforeAndStatusIdIn(cutoff, ids);
    }
}
