package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateReportTypeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPlateReportTypeTranslationDao extends JpaRepository<PlateReportTypeTranslation, Long> {

    List<PlateReportTypeTranslation> findByLocale(String locale);
}
