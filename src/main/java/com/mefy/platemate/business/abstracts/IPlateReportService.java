package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Plate;

import java.util.List;

public interface IPlateReportService {
    Result syncReportsForUserAndPlate(Long plateId, Long userId, List<String> reportTypeCodes);
}
