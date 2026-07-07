package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.business.utilities.plate.ReportTypeTranslationResolver;
import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.entities.concrete.PlateReportTypeTranslation;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.dto.PlateReviewAdminDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PlateReviewAdminMapper implements IMapper<PlateReview, PlateReviewAdminDto> {

    private final LocalizedEnumService localizedEnumService;
    private final ReportTypeTranslationResolver translationResolver;
    private final IPlateReportDao plateReportDao;

    @Override
    public PlateReviewAdminDto entityToDto(PlateReview entity) {
        if (entity == null) {
            return null;
        }
        return new PlateReviewAdminDto(
                entity.getId(),
                entity.getPlate() == null ? null : entity.getPlate().getPlateCode(),
                entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getUser() == null ? null : entity.getUser().getUsername(),
                entity.getRating(),
                entity.getComment(),
                entity.getStatusId(),
                entity.getStatusCode(),
                localizedEnumService != null ? localizedEnumService.label("review_status", entity.getStatusCode()) : null,
                entity.getModerationReason(),
                entity.getReportCount(),
                resolveReportTags(entity),
                entity.getUserAcceptedResponsibility(),
                entity.getResponsibilityPolicyVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    @Override
    public PlateReview dtoToEntity(PlateReviewAdminDto dto) {
        throw new UnsupportedOperationException("Mapping from PlateReviewAdminDto to entity is not supported");
    }

    private List<String> resolveReportTags(PlateReview review) {
        if (review.getPlate() == null || review.getPlate().getId() == null
                || review.getUser() == null || review.getUser().getId() == null) {
            return List.of();
        }
        Map<Long, PlateReportTypeTranslation> translations = translationResolver.loadTranslations();
        return plateReportDao
                .findByPlateIdAndUserIdAndActiveTrue(review.getPlate().getId(), review.getUser().getId())
                .stream()
                .map(report -> translationResolver.resolveLabel(report.getReportType(), translations))
                .toList();
    }
}
