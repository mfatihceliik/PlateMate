package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PremiumPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IPremiumPlanDao extends JpaRepository<PremiumPlan, Long> {
    List<PremiumPlan> findByActiveTrueOrderBySortOrderAsc();

    List<PremiumPlan> findAllByOrderBySortOrderAsc();

    Optional<PremiumPlan> findByPeriodIgnoreCase(String period);
}
