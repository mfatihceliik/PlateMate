package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;

public interface IDiscoveryService {
    DataResult<DiscoveryHomeDto> getHome(int limit, int cityLimit, int activityLimit);

    DataResult<PagedData<CityPlateActivityDto>> getCityPlates(Integer cityId, PaginationRequest paginationRequest);
}
