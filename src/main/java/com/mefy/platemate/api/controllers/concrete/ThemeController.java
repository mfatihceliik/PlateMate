package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IThemeController;
import com.mefy.platemate.business.abstracts.IThemeService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.ThemeCatalogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ThemeController implements IThemeController {

    private final IThemeService themeService;

    @Override
    public ResponseEntity<DataResult<ThemeCatalogDto>> getCatalog() {
        DataResult<ThemeCatalogDto> result = themeService.getCatalog();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
