package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ISocialPlatformService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ISocialPlatformLookupDao;
import com.mefy.platemate.entities.concrete.SocialPlatformLookup;
import com.mefy.platemate.entities.dto.SocialPlatformAdminDto;
import com.mefy.platemate.entities.dto.SocialPlatformDto;
import com.mefy.platemate.entities.dto.request.AddSocialPlatformRequest;
import com.mefy.platemate.entities.dto.request.UpdateSocialPlatformRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SocialPlatformManager implements ISocialPlatformService {
    private final ISocialPlatformLookupDao socialPlatformLookupDao;
    private final IMessageService messageService;

    @Override
    public DataResult<List<SocialPlatformDto>> getActivePlatforms() {
        List<SocialPlatformDto> data = socialPlatformLookupDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toPublicDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.SOCIAL_PLATFORMS_LISTED));
    }

    @Override
    public DataResult<List<SocialPlatformAdminDto>> getAllPlatforms() {
        List<SocialPlatformAdminDto> data = socialPlatformLookupDao.findAllByOrderBySortOrderAsc().stream()
                .map(this::toAdminDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.SOCIAL_PLATFORMS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<SocialPlatformAdminDto> addPlatform(AddSocialPlatformRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (socialPlatformLookupDao.existsByCodeIgnoreCase(normalizedCode)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.SOCIAL_PLATFORM_CODE_ALREADY_EXISTS));
        }

        LocalDateTime now = LocalDateTime.now();
        SocialPlatformLookup platform = new SocialPlatformLookup();
        platform.setCode(normalizedCode);
        applyFields(platform, request.getLabel(), request.getIconUrl(), request.getBaseUrl(), request.getBackgroundColorHex(), request.getIconTintColorHex(), request.getSortOrder());
        platform.setActive(true);
        platform.setCreatedAt(now);
        platform.setUpdatedAt(now);

        SocialPlatformLookup saved = socialPlatformLookupDao.save(platform);
        return new SuccessDataResult<>(toAdminDto(saved), messageService.getMessage(Messages.SOCIAL_PLATFORM_ADDED));
    }

    @Override
    @Transactional
    public DataResult<SocialPlatformAdminDto> updatePlatform(Long id, UpdateSocialPlatformRequest request) {
        SocialPlatformLookup existing = socialPlatformLookupDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.SOCIAL_PLATFORM_NOT_FOUND));
        }

        String normalizedCode = normalizeCode(request.getCode());
        SocialPlatformLookup other = socialPlatformLookupDao.findByCodeIgnoreCase(normalizedCode).orElse(null);
        if (other != null && !other.getId().equals(id)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.SOCIAL_PLATFORM_CODE_ALREADY_EXISTS));
        }

        applyFields(existing, request.getLabel(), request.getIconUrl(), request.getBaseUrl(), request.getBackgroundColorHex(), request.getIconTintColorHex(), request.getSortOrder());
        existing.setCode(normalizedCode);
        existing.setUpdatedAt(LocalDateTime.now());

        SocialPlatformLookup updated = socialPlatformLookupDao.save(existing);
        return new SuccessDataResult<>(toAdminDto(updated), messageService.getMessage(Messages.SOCIAL_PLATFORM_UPDATED));
    }

    @Override
    @Transactional
    public Result setPlatformActive(Long id, boolean active) {
        SocialPlatformLookup existing = socialPlatformLookupDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.SOCIAL_PLATFORM_NOT_FOUND));
        }

        existing.setActive(active);
        existing.setUpdatedAt(LocalDateTime.now());
        socialPlatformLookupDao.save(existing);

        return new SuccessResult(messageService.getMessage(Messages.SOCIAL_PLATFORM_STATUS_UPDATED));
    }

    private void applyFields(
            SocialPlatformLookup platform,
            String label,
            String iconUrl,
            String baseUrl,
            String backgroundColorHex,
            String iconTintColorHex,
            Integer sortOrder
    ) {
        platform.setLabel(label.trim());
        platform.setIconUrl(iconUrl == null ? null : iconUrl.trim());
        platform.setBaseUrl(baseUrl == null ? null : baseUrl.trim());
        platform.setBackgroundColorHex(backgroundColorHex == null ? null : backgroundColorHex.trim().toUpperCase(Locale.ROOT));
        platform.setIconTintColorHex(iconTintColorHex == null ? null : iconTintColorHex.trim().toUpperCase(Locale.ROOT));
        platform.setSortOrder(sortOrder);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private SocialPlatformDto toPublicDto(SocialPlatformLookup platform) {
        return new SocialPlatformDto(
                platform.getId(),
                platform.getCode(),
                platform.getLabel(),
                platform.getIconUrl(),
                platform.getBaseUrl(),
                platform.getBackgroundColorHex(),
                platform.getIconTintColorHex(),
                platform.getSortOrder()
        );
    }

    private SocialPlatformAdminDto toAdminDto(SocialPlatformLookup platform) {
        return new SocialPlatformAdminDto(
                platform.getId(),
                platform.getCode(),
                platform.getLabel(),
                platform.getIconUrl(),
                platform.getBaseUrl(),
                platform.getBackgroundColorHex(),
                platform.getIconTintColorHex(),
                platform.getSortOrder(),
                platform.getActive(),
                platform.getCreatedAt(),
                platform.getUpdatedAt()
        );
    }
}
