package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IPremiumController;
import com.mefy.platemate.business.abstracts.IPremiumService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.PremiumCatalogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PremiumController implements IPremiumController {

    private final IPremiumService premiumService;

    @Override
    public ResponseEntity<DataResult<PremiumCatalogDto>> getCatalog() {
        DataResult<PremiumCatalogDto> result = premiumService.getCatalog();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
