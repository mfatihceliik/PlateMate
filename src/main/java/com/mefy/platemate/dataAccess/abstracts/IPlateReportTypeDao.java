package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IPlateReportTypeDao extends JpaRepository<PlateReportType, Long> {
    List<PlateReportType> findByActiveTrueOrderBySortOrderAsc();

    List<PlateReportType> findAllByOrderBySortOrderAsc();

    List<PlateReportType> findByCodeInAndActiveTrue(Collection<String> codes);

    boolean existsByCode(String code);

    Optional<PlateReportType> findByCode(String code);
}
