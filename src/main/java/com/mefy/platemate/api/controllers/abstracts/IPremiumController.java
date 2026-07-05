package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PremiumCatalogDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/premium")
public interface IPremiumController {

    @GetMapping
    ResponseEntity<DataResult<PremiumCatalogDto>> getCatalog();
}
