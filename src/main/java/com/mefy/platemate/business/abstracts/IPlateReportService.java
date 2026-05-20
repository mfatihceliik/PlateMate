package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Plate;

import java.util.List;

public interface IPlateReportService {
    Result syncReportsForUserAndPlate(Plate plate, Long userId, List<String> reportTypeCodes);
}
