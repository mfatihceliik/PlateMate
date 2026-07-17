package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityPlateActivityDto;
import com.mefy.platemate.entities.dto.DiscoveryHomeDto;
import com.mefy.platemate.entities.dto.DiscoveryPlateCardDto;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/discovery")
public interface IDiscoveryController {

    @GetMapping("/tabs")
    ResponseEntity<DataResult<List<DiscoveryTabOptionDto>>> getTabOptions();

    @GetMapping("/home")
    ResponseEntity<DataResult<DiscoveryHomeDto>> getHome(
            @RequestAttribute("userId") Long userId,
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

    @GetMapping("/tabs/{tabType}/plates")
    ResponseEntity<DataResult<PagedData<DiscoveryPlateCardDto>>> getTabPlates(
            @RequestAttribute("userId") Long userId,
            @PathVariable String tabType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) java.util.List<Integer> cityIds,
            @RequestParam(required = false) String reportTypeCode,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Integer windowDays
    );
}
