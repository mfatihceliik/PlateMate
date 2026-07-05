package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.AccentColor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAccentColorDao extends JpaRepository<AccentColor, Long> {
    List<AccentColor> findByActiveTrueOrderBySortOrderAsc();

    List<AccentColor> findAllByOrderBySortOrderAsc();

    boolean existsByHexIgnoreCase(String hex);
}
