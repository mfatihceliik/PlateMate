package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.ISocialPlatformService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.SocialPlatformAdminMapper;
import com.mefy.platemate.business.utilities.mappers.SocialPlatformMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.ISocialPlatformLookupDao;
import com.mefy.platemate.entities.concrete.SocialPlatformLookup;
import com.mefy.platemate.entities.concrete.SocialPlatformTranslation;
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
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialPlatformManager implements ISocialPlatformService {
    private final ISocialPlatformLookupDao socialPlatformLookupDao;
    private final IMessageService messageService;
    private final SocialPlatformMapper socialPlatformMapper;
    private final SocialPlatformAdminMapper socialPlatformAdminMapper;

    @Override
    public DataResult<List<SocialPlatformDto>> getActivePlatforms() {
        List<SocialPlatformDto> data = socialPlatformLookupDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(socialPlatformMapper::entityToDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.SOCIAL_PLATFORMS_LISTED));
    }

    @Override
    public DataResult<List<SocialPlatformAdminDto>> getAllPlatforms() {
        List<SocialPlatformAdminDto> data = socialPlatformLookupDao.findAllByOrderBySortOrderAsc().stream()
                .map(socialPlatformAdminMapper::entityToDto)
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
        applyFields(platform, request.getIconUrl(), request.getBaseUrl(), request.getBackgroundColorHex(), request.getIconTintColorHex(), request.getSortOrder());
        platform.setActive(true);
        platform.setCreatedAt(now);
        platform.setUpdatedAt(now);
        
        applyTranslations(platform, request.getLabels());

        SocialPlatformLookup saved = socialPlatformLookupDao.save(platform);
        return new SuccessDataResult<>(socialPlatformAdminMapper.entityToDto(saved), messageService.getMessage(Messages.SOCIAL_PLATFORM_ADDED));
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

        applyFields(existing, request.getIconUrl(), request.getBaseUrl(), request.getBackgroundColorHex(), request.getIconTintColorHex(), request.getSortOrder());
        existing.setCode(normalizedCode);
        existing.setUpdatedAt(LocalDateTime.now());
        
        applyTranslations(existing, request.getLabels());

        SocialPlatformLookup updated = socialPlatformLookupDao.save(existing);
        return new SuccessDataResult<>(socialPlatformAdminMapper.entityToDto(updated), messageService.getMessage(Messages.SOCIAL_PLATFORM_UPDATED));
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
            String iconUrl,
            String baseUrl,
            String backgroundColorHex,
            String iconTintColorHex,
            Integer sortOrder
    ) {
        platform.setIconUrl(iconUrl == null ? null : iconUrl.trim());
        platform.setBaseUrl(baseUrl == null ? null : baseUrl.trim());
        platform.setBackgroundColorHex(backgroundColorHex == null ? null : backgroundColorHex.trim().toUpperCase(Locale.ROOT));
        platform.setIconTintColorHex(iconTintColorHex == null ? null : iconTintColorHex.trim().toUpperCase(Locale.ROOT));
        platform.setSortOrder(sortOrder);
    }
    
    private void applyTranslations(SocialPlatformLookup platform, Map<String, String> labels) {
        if (labels == null) return;

        for (Map.Entry<String, String> entry : labels.entrySet()) {
            String locale = entry.getKey().toLowerCase(Locale.ROOT);
            String label = entry.getValue().trim();

            Optional<SocialPlatformTranslation> existingOpt = platform.getTranslations().stream()
                    .filter(t -> t.getLocale().equals(locale))
                    .findFirst();

            if (existingOpt.isPresent()) {
                SocialPlatformTranslation existing = existingOpt.get();
                existing.setLabel(label);
                existing.setUpdatedAt(LocalDateTime.now());
            } else {
                SocialPlatformTranslation newTranslation = new SocialPlatformTranslation();
                newTranslation.setSocialPlatform(platform);
                newTranslation.setLocale(locale);
                newTranslation.setLabel(label);
                platform.getTranslations().add(newTranslation);
            }
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

}
