package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/discovery")
public interface IDiscoveryController {

    @GetMapping("/home")
    ResponseEntity<DataResult<DiscoveryHomeDto>> getHome(
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "5") int cityLimit,
            @RequestParam(defaultValue = "20") int activityLimit
    );

    @GetMapping("/cities/{cityId}/plates")
    ResponseEntity<DataResult<PagedData<CityPlateActivityDto>>> getCityPlates(
            @PathVariable Integer cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
}
