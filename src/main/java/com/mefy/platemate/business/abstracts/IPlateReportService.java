package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Plate;

import java.util.List;

import com.mefy.platemate.entities.dto.request.SyncPlateReportsRequest;

public interface IPlateReportService {
    Result syncReportsForUserAndPlate(Long plateId, Long userId, List<String> reportTypeCodes);
    Result syncReports(String plateCode, Long currentUserId, SyncPlateReportsRequest request);
}
