package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.AccentColorAdminDto;
import com.mefy.platemate.entities.dto.ThemeCatalogDto;
import com.mefy.platemate.entities.dto.request.AccentColorRequest;

import java.util.List;

public interface IThemeService {
    // Public read (active colors + grid size).
    DataResult<ThemeCatalogDto> getCatalog();

    // Admin — colors (CRUD).
    DataResult<List<AccentColorAdminDto>> getAllColors();

    DataResult<AccentColorAdminDto> addColor(AccentColorRequest request);

    DataResult<AccentColorAdminDto> updateColor(Long id, AccentColorRequest request);

    Result setColorActive(Long id, boolean active);

    // Admin — grid size.
    Result updateGridSize(int gridSize);
}
