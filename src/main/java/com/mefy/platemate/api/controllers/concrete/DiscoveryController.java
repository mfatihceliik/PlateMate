package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IDiscoveryController;
import com.mefy.platemate.business.abstracts.IDiscoveryService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.request.DiscoveryTabFeedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiscoveryController implements IDiscoveryController {

    private final IDiscoveryService discoveryService;

    @Override
    public ResponseEntity<DataResult<DiscoveryHomeDto>> getHome(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "5") int cityLimit,
            @RequestParam(defaultValue = "20") int activityLimit
    ) {
        DataResult<DiscoveryHomeDto> result = discoveryService.getHome(userId, limit, cityLimit, activityLimit);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PagedData<CityPlateActivityDto>>> getCityPlates(
            @PathVariable Integer cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaginationRequest paginationRequest = PaginationRequest.of(page, size);
        DataResult<PagedData<CityPlateActivityDto>> result = discoveryService.getCityPlates(cityId, paginationRequest);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PagedData<DiscoveryPlateCardDto>>> getTabPlates(
            @RequestAttribute("userId") Long userId,
            @PathVariable String tabType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) java.util.List<Integer> cityIds,
            @RequestParam(required = false) String reportTypeCode,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer windowDays
    ) {
        PaginationRequest paginationRequest = PaginationRequest.of(page, size);
        DiscoveryTabFeedRequest filter = new DiscoveryTabFeedRequest(cityIds, reportTypeCode, minRating, windowDays);
        DataResult<PagedData<DiscoveryPlateCardDto>> result =
                discoveryService.getTabPlates(userId, tabType, filter, paginationRequest);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
