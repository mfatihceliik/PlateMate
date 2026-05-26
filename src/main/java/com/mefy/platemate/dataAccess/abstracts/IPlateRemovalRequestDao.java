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
    Page<PlateRemovalRequest> findByStatusOrderByCreatedAtAsc(PlateRemovalRequestStatus status, Pageable pageable);

    Page<PlateRemovalRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("""
            delete from PlateRemovalRequest pr
            where pr.createdAt < :cutoff
              and pr.status in :resolvedStatuses
            """)
    int deleteResolvedByCreatedAtBefore(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("resolvedStatuses") List<PlateRemovalRequestStatus> resolvedStatuses
    );
}
