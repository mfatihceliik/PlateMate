package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PremiumFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPremiumFeatureDao extends JpaRepository<PremiumFeature, Long> {
    List<PremiumFeature> findByActiveTrueOrderBySortOrderAsc();

    List<PremiumFeature> findAllByOrderBySortOrderAsc();
}
