package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateReportTypeAdminDto;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import com.mefy.platemate.entities.dto.request.AddPlateReportTypeRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeRequest;

import java.util.List;

public interface IPlateReportTypeService {
    DataResult<List<PlateReportTypeDto>> getActiveReportTypes();

    DataResult<List<PlateReportTypeAdminDto>> getAllReportTypes();

    DataResult<PlateReportTypeAdminDto> addReportType(AddPlateReportTypeRequest request);

    DataResult<PlateReportTypeAdminDto> updateReportType(Long id, UpdatePlateReportTypeRequest request);

    Result setReportTypeActive(Long id, boolean active);
}
