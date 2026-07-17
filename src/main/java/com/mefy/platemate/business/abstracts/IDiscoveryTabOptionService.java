package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionAdminDto;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionDto;
import com.mefy.platemate.entities.dto.request.AddDiscoveryTabOptionRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionRequest;

import java.util.List;

public interface IDiscoveryTabOptionService {
    DataResult<List<DiscoveryTabOptionDto>> getActiveTabOptions();

    DataResult<List<DiscoveryTabOptionAdminDto>> getAllTabOptions();

    DataResult<DiscoveryTabOptionAdminDto> addTabOption(AddDiscoveryTabOptionRequest request);

    DataResult<DiscoveryTabOptionAdminDto> updateTabOption(Long id, UpdateDiscoveryTabOptionRequest request);

    Result setTabOptionActive(Long id, boolean active);
}
