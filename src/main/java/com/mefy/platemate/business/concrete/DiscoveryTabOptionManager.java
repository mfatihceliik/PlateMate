package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IDiscoveryTabOptionService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IDiscoveryTabOptionDao;
import com.mefy.platemate.entities.concrete.DiscoveryTabOption;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionAdminDto;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionDto;
import com.mefy.platemate.entities.dto.DiscoveryTabType;
import com.mefy.platemate.entities.dto.request.AddDiscoveryTabOptionRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DiscoveryTabOptionManager implements IDiscoveryTabOptionService {

    private static final String ENUM_CATEGORY = "discovery_tab_option";

    private final IDiscoveryTabOptionDao discoveryTabOptionDao;
    private final LocalizedEnumService localizedEnumService;
    private final IMessageService messageService;

    @Override
    public DataResult<List<DiscoveryTabOptionDto>> getActiveTabOptions() {
        List<DiscoveryTabOptionDto> data = discoveryTabOptionDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toPublicDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.DISCOVERY_TAB_OPTIONS_LISTED));
    }

    @Override
    public DataResult<List<DiscoveryTabOptionAdminDto>> getAllTabOptions() {
        List<DiscoveryTabOptionAdminDto> data = discoveryTabOptionDao.findAllByOrderBySortOrderAsc().stream()
                .map(this::toAdminDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.DISCOVERY_TAB_OPTIONS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<DiscoveryTabOptionAdminDto> addTabOption(AddDiscoveryTabOptionRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (!isKnownTabType(normalizedCode)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.VALIDATION_DISCOVERY_TAB_OPTION_CODE_INVALID));
        }
        if (discoveryTabOptionDao.existsByCode(normalizedCode)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_ALREADY_EXISTS));
        }

        LocalDateTime now = LocalDateTime.now();
        DiscoveryTabOption option = new DiscoveryTabOption();
        option.setCode(normalizedCode);
        option.setLabel(request.getLabel().trim());
        option.setSortOrder(request.getSortOrder());
        option.setActive(true);
        option.setCreatedAt(now);
        option.setUpdatedAt(now);
        DiscoveryTabOption saved = discoveryTabOptionDao.save(option);

        return new SuccessDataResult<>(toAdminDto(saved), messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_ADDED));
    }

    @Override
    @Transactional
    public DataResult<DiscoveryTabOptionAdminDto> updateTabOption(Long id, UpdateDiscoveryTabOptionRequest request) {
        DiscoveryTabOption existing = discoveryTabOptionDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_NOT_FOUND));
        }

        String normalizedCode = normalizeCode(request.getCode());
        if (!isKnownTabType(normalizedCode)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.VALIDATION_DISCOVERY_TAB_OPTION_CODE_INVALID));
        }
        DiscoveryTabOption otherWithCode = discoveryTabOptionDao.findByCode(normalizedCode).orElse(null);
        if (otherWithCode != null && !otherWithCode.getId().equals(id)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_ALREADY_EXISTS));
        }

        existing.setCode(normalizedCode);
        existing.setLabel(request.getLabel().trim());
        existing.setSortOrder(request.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());
        DiscoveryTabOption updated = discoveryTabOptionDao.save(existing);

        return new SuccessDataResult<>(toAdminDto(updated), messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_UPDATED));
    }

    @Override
    @Transactional
    public Result setTabOptionActive(Long id, boolean active) {
        DiscoveryTabOption existing = discoveryTabOptionDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_NOT_FOUND));
        }

        existing.setActive(active);
        existing.setUpdatedAt(LocalDateTime.now());
        discoveryTabOptionDao.save(existing);

        return new SuccessResult(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_STATUS_UPDATED));
    }

    private boolean isKnownTabType(String code) {
        try {
            DiscoveryTabType.valueOf(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private DiscoveryTabOptionDto toPublicDto(DiscoveryTabOption option) {
        String label = localizedEnumService.label(ENUM_CATEGORY, option.getCode(), option.getLabel());
        return new DiscoveryTabOptionDto(option.getCode(), label, option.getSortOrder());
    }

    private DiscoveryTabOptionAdminDto toAdminDto(DiscoveryTabOption option) {
        return new DiscoveryTabOptionAdminDto(
                option.getId(),
                option.getCode(),
                option.getLabel(),
                option.getSortOrder(),
                option.isActive(),
                option.getCreatedAt(),
                option.getUpdatedAt()
        );
    }
}
