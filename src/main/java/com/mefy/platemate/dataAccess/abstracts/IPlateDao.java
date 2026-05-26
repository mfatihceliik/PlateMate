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

    Optional<Plate> findByPlateCodeAndStatus(String plateCode, PlateStatus status);

    List<Plate> findByStatusInOrderByUpdatedAtDesc(Collection<PlateStatus> statuses, Pageable pageable);

    Page<Plate> findByStatusIn(Collection<PlateStatus> statuses, Pageable pageable);

    List<Plate> findByReviewCountGreaterThanEqual(int reviewCount, Pageable pageable);
}
