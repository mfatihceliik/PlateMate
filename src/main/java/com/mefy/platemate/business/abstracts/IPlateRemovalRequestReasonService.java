package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonAdminDto;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateRemovalRequestReasonRequest;

import java.util.List;

public interface IPlateRemovalRequestReasonService {
    DataResult<List<PlateRemovalRequestReasonDto>> getActiveReasons();

    DataResult<List<PlateRemovalRequestReasonAdminDto>> getAllReasons();

    DataResult<PlateRemovalRequestReasonAdminDto> addReason(AddPlateRemovalRequestReasonRequest request);

    DataResult<PlateRemovalRequestReasonAdminDto> updateReason(Long id, UpdatePlateRemovalRequestReasonRequest request);

    Result setReasonActive(Long id, boolean active);
}
