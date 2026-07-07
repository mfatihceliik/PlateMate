package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IThemeService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.AccentColorAdminMapper;
import com.mefy.platemate.business.utilities.mappers.AccentColorMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IAccentColorDao;
import com.mefy.platemate.dataAccess.abstracts.IThemeConfigDao;
import com.mefy.platemate.entities.concrete.AccentColor;
import com.mefy.platemate.entities.concrete.ThemeConfig;
import com.mefy.platemate.entities.dto.AccentColorAdminDto;
import com.mefy.platemate.entities.dto.AccentColorDto;
import com.mefy.platemate.entities.dto.ThemeCatalogDto;
import com.mefy.platemate.entities.dto.request.AccentColorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ThemeManager implements IThemeService {
    private static final long CONFIG_ID = 1L;
    private static final int DEFAULT_GRID_SIZE = 4;

    private final IAccentColorDao accentColorDao;
    private final IThemeConfigDao themeConfigDao;
    private final IMessageService messageService;
    private final AccentColorMapper accentColorMapper;
    private final AccentColorAdminMapper accentColorAdminMapper;

    @Override
    public DataResult<ThemeCatalogDto> getCatalog() {
        List<AccentColorDto> colors = accentColorDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(accentColorMapper::entityToDto)
                .toList();
        int gridSize = themeConfigDao.findById(CONFIG_ID)
                .map(ThemeConfig::getGridSize)
                .orElse(DEFAULT_GRID_SIZE);
        ThemeCatalogDto catalog = new ThemeCatalogDto(gridSize, colors);
        return new SuccessDataResult<>(catalog, messageService.getMessage(Messages.THEME_CATALOG_LOADED));
    }

    @Override
    public DataResult<List<AccentColorAdminDto>> getAllColors() {
        List<AccentColorAdminDto> data = accentColorDao.findAllByOrderBySortOrderAsc().stream()
                .map(accentColorAdminMapper::entityToDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.ACCENT_COLORS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<AccentColorAdminDto> addColor(AccentColorRequest request) {
        String hex = normalizeHex(request.getHex());
        if (accentColorDao.existsByHexIgnoreCase(hex)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.ACCENT_COLOR_ALREADY_EXISTS));
        }
        LocalDateTime now = LocalDateTime.now();
        AccentColor color = new AccentColor();
        color.setHex(hex);
        color.setSortOrder(request.getSortOrder());
        color.setActive(true);
        color.setCreatedAt(now);
        color.setUpdatedAt(now);
        AccentColor saved = accentColorDao.save(color);
        return new SuccessDataResult<>(accentColorAdminMapper.entityToDto(saved), messageService.getMessage(Messages.ACCENT_COLOR_ADDED));
    }

    @Override
    @Transactional
    public DataResult<AccentColorAdminDto> updateColor(Long id, AccentColorRequest request) {
        AccentColor existing = accentColorDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.ACCENT_COLOR_NOT_FOUND));
        }
        String hex = normalizeHex(request.getHex());
        if (accentColorDao.existsByHexIgnoreCase(hex) && !hex.equalsIgnoreCase(existing.getHex())) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.ACCENT_COLOR_ALREADY_EXISTS));
        }
        existing.setHex(hex);
        existing.setSortOrder(request.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());
        AccentColor updated = accentColorDao.save(existing);
        return new SuccessDataResult<>(accentColorAdminMapper.entityToDto(updated), messageService.getMessage(Messages.ACCENT_COLOR_UPDATED));
    }

    @Override
    @Transactional
    public Result setColorActive(Long id, boolean active) {
        AccentColor existing = accentColorDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.ACCENT_COLOR_NOT_FOUND));
        }
        existing.setActive(active);
        existing.setUpdatedAt(LocalDateTime.now());
        accentColorDao.save(existing);
        return new SuccessResult(messageService.getMessage(Messages.ACCENT_COLOR_STATUS_UPDATED));
    }

    @Override
    @Transactional
    public Result updateGridSize(int gridSize) {
        LocalDateTime now = LocalDateTime.now();
        ThemeConfig config = themeConfigDao.findById(CONFIG_ID).orElseGet(() -> {
            ThemeConfig created = new ThemeConfig();
            created.setId(CONFIG_ID);
            created.setCreatedAt(now);
            return created;
        });
        config.setGridSize(gridSize);
        config.setUpdatedAt(now);
        themeConfigDao.save(config);
        return new SuccessResult(messageService.getMessage(Messages.THEME_GRID_SIZE_UPDATED));
    }

    private String normalizeHex(String hex) {
        return hex.trim().toUpperCase(Locale.ROOT);
    }

}
