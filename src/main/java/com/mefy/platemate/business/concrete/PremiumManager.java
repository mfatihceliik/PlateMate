package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPremiumService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.mappers.PremiumFeatureAdminMapper;
import com.mefy.platemate.business.utilities.mappers.PremiumFeatureMapper;
import com.mefy.platemate.business.utilities.mappers.PremiumPlanAdminMapper;
import com.mefy.platemate.business.utilities.mappers.PremiumPlanMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPremiumFeatureDao;
import com.mefy.platemate.dataAccess.abstracts.IPremiumPlanDao;
import com.mefy.platemate.entities.concrete.PremiumFeature;
import com.mefy.platemate.entities.concrete.PremiumFeatureTranslation;
import com.mefy.platemate.entities.concrete.PremiumPlan;
import com.mefy.platemate.entities.concrete.PremiumPlanTranslation;
import com.mefy.platemate.entities.dto.PremiumCatalogDto;
import com.mefy.platemate.entities.dto.PremiumFeatureAdminDto;
import com.mefy.platemate.entities.dto.PremiumFeatureDto;
import com.mefy.platemate.entities.dto.PremiumPlanAdminDto;
import com.mefy.platemate.entities.dto.PremiumPlanDto;
import com.mefy.platemate.entities.dto.request.AddPremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumPlanRequest;
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
public class PremiumManager implements IPremiumService {
    private final IPremiumPlanDao premiumPlanDao;
    private final IPremiumFeatureDao premiumFeatureDao;
    private final IMessageService messageService;
    private final PremiumPlanMapper premiumPlanMapper;
    private final PremiumPlanAdminMapper premiumPlanAdminMapper;
    private final PremiumFeatureMapper premiumFeatureMapper;
    private final PremiumFeatureAdminMapper premiumFeatureAdminMapper;

    // ---- Public ----

    @Override
    public DataResult<PremiumCatalogDto> getCatalog() {
        List<PremiumPlanDto> plans = premiumPlanDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(premiumPlanMapper::entityToDto)
                .toList();
        List<PremiumFeatureDto> features = premiumFeatureDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(premiumFeatureMapper::entityToDto)
                .toList();
        PremiumCatalogDto catalog = new PremiumCatalogDto(plans, features);
        return new SuccessDataResult<>(catalog, messageService.getMessage(Messages.PREMIUM_CATALOG_LOADED));
    }

    // ---- Admin: plans ----

    @Override
    public DataResult<List<PremiumPlanAdminDto>> getAllPlans() {
        List<PremiumPlanAdminDto> data = premiumPlanDao.findAllByOrderBySortOrderAsc().stream()
                .map(premiumPlanAdminMapper::entityToDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.PREMIUM_PLANS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<PremiumPlanAdminDto> updatePlan(Long id, UpdatePremiumPlanRequest request) {
        PremiumPlan existing = premiumPlanDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PREMIUM_PLAN_NOT_FOUND));
        }

        existing.setAmount(request.getAmount());
        existing.setCurrency(request.getCurrency().trim().toUpperCase(Locale.ROOT));
        existing.setDiscountPercent(request.getDiscountPercent());
        existing.setSortOrder(request.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());
        
        applyPlanTranslations(existing, request.getTitles(), request.getDescriptions());

        PremiumPlan updated = premiumPlanDao.save(existing);
        return new SuccessDataResult<>(premiumPlanAdminMapper.entityToDto(updated), messageService.getMessage(Messages.PREMIUM_PLAN_UPDATED));
    }

    @Override
    @Transactional
    public Result setPlanActive(Long id, boolean active) {
        PremiumPlan existing = premiumPlanDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.PREMIUM_PLAN_NOT_FOUND));
        }
        existing.setActive(active);
        existing.setUpdatedAt(LocalDateTime.now());
        premiumPlanDao.save(existing);
        return new SuccessResult(messageService.getMessage(Messages.PREMIUM_PLAN_STATUS_UPDATED));
    }

    // ---- Admin: features ----

    @Override
    public DataResult<List<PremiumFeatureAdminDto>> getAllFeatures() {
        List<PremiumFeatureAdminDto> data = premiumFeatureDao.findAllByOrderBySortOrderAsc().stream()
                .map(premiumFeatureAdminMapper::entityToDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.PREMIUM_FEATURES_LISTED));
    }

    @Override
    @Transactional
    public DataResult<PremiumFeatureAdminDto> addFeature(AddPremiumFeatureRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PremiumFeature feature = new PremiumFeature();
        feature.setIconKey(request.getIconKey().trim().toLowerCase(Locale.ROOT));
        feature.setSortOrder(request.getSortOrder());
        feature.setActive(true);
        feature.setCreatedAt(now);
        feature.setUpdatedAt(now);
        
        applyFeatureTranslations(feature, request.getTitles(), request.getSubtitles());

        PremiumFeature saved = premiumFeatureDao.save(feature);
        return new SuccessDataResult<>(premiumFeatureAdminMapper.entityToDto(saved), messageService.getMessage(Messages.PREMIUM_FEATURE_ADDED));
    }

    @Override
    @Transactional
    public DataResult<PremiumFeatureAdminDto> updateFeature(Long id, UpdatePremiumFeatureRequest request) {
        PremiumFeature existing = premiumFeatureDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PREMIUM_FEATURE_NOT_FOUND));
        }
        existing.setIconKey(request.getIconKey().trim().toLowerCase(Locale.ROOT));
        existing.setSortOrder(request.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());
        
        applyFeatureTranslations(existing, request.getTitles(), request.getSubtitles());

        PremiumFeature updated = premiumFeatureDao.save(existing);
        return new SuccessDataResult<>(premiumFeatureAdminMapper.entityToDto(updated), messageService.getMessage(Messages.PREMIUM_FEATURE_UPDATED));
    }

    @Override
    @Transactional
    public Result setFeatureActive(Long id, boolean active) {
        PremiumFeature existing = premiumFeatureDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.PREMIUM_FEATURE_NOT_FOUND));
        }
        existing.setActive(active);
        existing.setUpdatedAt(LocalDateTime.now());
        premiumFeatureDao.save(existing);
        return new SuccessResult(messageService.getMessage(Messages.PREMIUM_FEATURE_STATUS_UPDATED));
    }

    // ---- Mapping / helpers ----

    private void applyFeatureTranslations(PremiumFeature feature, Map<String, String> titles, Map<String, String> subtitles) {
        if (titles == null) return;
        
        for (Map.Entry<String, String> entry : titles.entrySet()) {
            String locale = entry.getKey().toLowerCase(Locale.ROOT);
            String title = entry.getValue().trim();
            String subtitle = subtitles != null && subtitles.containsKey(locale) ? blankToNull(subtitles.get(locale)) : null;

            Optional<PremiumFeatureTranslation> existingOpt = feature.getTranslations().stream()
                    .filter(t -> t.getLocale().equals(locale))
                    .findFirst();

            if (existingOpt.isPresent()) {
                PremiumFeatureTranslation existing = existingOpt.get();
                existing.setTitle(title);
                existing.setSubtitle(subtitle);
                existing.setUpdatedAt(LocalDateTime.now());
            } else {
                PremiumFeatureTranslation newTranslation = new PremiumFeatureTranslation();
                newTranslation.setPremiumFeature(feature);
                newTranslation.setLocale(locale);
                newTranslation.setTitle(title);
                newTranslation.setSubtitle(subtitle);
                feature.getTranslations().add(newTranslation);
            }
        }
    }

    private void applyPlanTranslations(PremiumPlan plan, Map<String, String> titles, Map<String, String> descriptions) {
        if (titles == null) return;

        for (Map.Entry<String, String> entry : titles.entrySet()) {
            String locale = entry.getKey().toLowerCase(Locale.ROOT);
            String title = entry.getValue().trim();
            String description = descriptions != null && descriptions.containsKey(locale) ? blankToNull(descriptions.get(locale)) : null;

            Optional<PremiumPlanTranslation> existingOpt = plan.getTranslations().stream()
                    .filter(t -> t.getLocale().equals(locale))
                    .findFirst();

            if (existingOpt.isPresent()) {
                PremiumPlanTranslation existing = existingOpt.get();
                existing.setTitle(title);
                existing.setDescription(description);
                existing.setUpdatedAt(LocalDateTime.now());
            } else {
                PremiumPlanTranslation newTranslation = new PremiumPlanTranslation();
                newTranslation.setPremiumPlan(plan);
                newTranslation.setLocale(locale);
                newTranslation.setTitle(title);
                newTranslation.setDescription(description);
                plan.getTranslations().add(newTranslation);
            }
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
