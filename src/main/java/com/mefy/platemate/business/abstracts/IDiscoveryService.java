package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.request.DiscoveryTabFeedRequest;

public interface IDiscoveryService {
    DataResult<DiscoveryHomeDto> getHome(Long userId, int limit, int cityLimit, int activityLimit);

    DataResult<PagedData<CityPlateActivityDto>> getCityPlates(Integer cityId, PaginationRequest paginationRequest);

    DataResult<PagedData<DiscoveryPlateCardDto>> getTabPlates(
            Long userId,
            String tabType,
            DiscoveryTabFeedRequest filter,
            PaginationRequest paginationRequest
    );
}
