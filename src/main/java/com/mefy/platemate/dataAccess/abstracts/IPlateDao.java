package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Plate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPlateDao extends JpaRepository<Plate, Long> {
    Optional<Plate> findByPlateCode(String plateCode);

    List<Plate> findByReviewCountGreaterThanEqual(int reviewCount, Pageable pageable);
}
