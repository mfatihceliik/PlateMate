package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.Report;
import com.mefy.platemate.entities.dto.ReportDto;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper implements ModelMapperService<Report, ReportDto> {

    @Override
    public ReportDto entityToDto(Report entity) {
        if (entity == null) return null;
        ReportDto dto = new ReportDto();
        dto.setId(entity.getId());
        
        if (entity.getReporter() != null) {
            dto.setReporterId(entity.getReporter().getId());
            dto.setReporterUsername(entity.getReporter().getUsername());
        }
        if (entity.getReportedUser() != null) {
            dto.setReportedUserId(entity.getReportedUser().getId());
            dto.setReportedUserUsername(entity.getReportedUser().getUsername());
        }
        
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    @Override
    public Report dtoToEntity(ReportDto dto) {
        return null;
    }
}
