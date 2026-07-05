package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.ThemeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IThemeConfigDao extends JpaRepository<ThemeConfig, Long> {
}
