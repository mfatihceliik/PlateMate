package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateRemovalRequestReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPlateRemovalRequestReasonDao extends JpaRepository<PlateRemovalRequestReason, Long> {
    List<PlateRemovalRequestReason> findByActiveTrueOrderBySortOrderAsc();
    List<PlateRemovalRequestReason> findAllByOrderBySortOrderAsc();
    Optional<PlateRemovalRequestReason> findByCode(String code);
    boolean existsByCode(String code);
}
