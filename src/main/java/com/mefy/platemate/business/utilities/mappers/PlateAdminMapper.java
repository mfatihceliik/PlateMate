package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.dto.PlateAdminDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlateAdminMapper implements IMapper<Plate, PlateAdminDto> {

    private final LocalizedEnumService localizedEnumService;
    private final IPlateReportDao plateReportDao;

    @Override
    public PlateAdminDto entityToDto(Plate entity) {
        if (entity == null) {
            return null;
        }
        
        long reportCount = plateReportDao.countByPlateIdAndActiveTrue(entity.getId());
        
        return new PlateAdminDto(
                entity.getId(),
                entity.getPlateCode(),
                entity.getStatusId(),
                entity.getStatusCode(),
                localizedEnumService != null ? localizedEnumService.label("plate_status", entity.getStatusCode()) : null,
                entity.getHiddenReason(),
                entity.getReviewCount(),
                (int) reportCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    @Override
    public Plate dtoToEntity(PlateAdminDto dto) {
        throw new UnsupportedOperationException("Mapping from PlateAdminDto to Plate is not supported");
    }
}
