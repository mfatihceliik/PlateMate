package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IDiscoveryController;
import com.mefy.platemate.business.abstracts.IDiscoveryService;
import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiscoveryController implements IDiscoveryController {

    private final IDiscoveryService discoveryService;

    @Override
    public ResponseEntity<DataResult<DiscoveryHomeDto>> getHome(
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "5") int cityLimit,
            @RequestParam(defaultValue = "20") int activityLimit
    ) {
        DataResult<DiscoveryHomeDto> result = discoveryService.getHome(limit, cityLimit, activityLimit);
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
}
