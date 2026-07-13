package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IPlateDao extends JpaRepository<Plate, Long> {
    Optional<Plate> findByPlateCode(String plateCode);

    Optional<Plate> findByPlateCodeAndStatusId(String plateCode, Long statusId);

    List<Plate> findByStatusIdInOrderByUpdatedAtDesc(Collection<Long> statusIds, Pageable pageable);

    Page<Plate> findByStatusIdIn(Collection<Long> statusIds, Pageable pageable);

    List<Plate> findByReviewCountGreaterThanEqual(int reviewCount, Pageable pageable);

    long countByStatusIdIn(Collection<Long> statusIds);

    default Optional<Plate> findByPlateCodeAndStatus(String plateCode, PlateStatus status) {
        return findByPlateCodeAndStatusId(plateCode, status == null ? null : status.getId());
    }

    default List<Plate> findByStatusInOrderByUpdatedAtDesc(Collection<PlateStatus> statuses, Pageable pageable) {
        List<Long> ids = statuses == null ? List.of() : statuses.stream().map(PlateStatus::getId).toList();
        return findByStatusIdInOrderByUpdatedAtDesc(ids, pageable);
    }

    default Page<Plate> findByStatusIn(Collection<PlateStatus> statuses, Pageable pageable) {
        List<Long> ids = statuses == null ? List.of() : statuses.stream().map(PlateStatus::getId).toList();
        return findByStatusIdIn(ids, pageable);
    }
}
