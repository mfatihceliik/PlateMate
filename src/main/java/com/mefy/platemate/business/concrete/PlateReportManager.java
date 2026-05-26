package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.concrete.PlateReportTypePolicyService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.concrete.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlateReportManager implements IPlateReportService {

    private final IPlateReportDao plateReportDao;
    private final IPlateReportTypeDao plateReportTypeDao;
    private final PlateReportTypePolicyService plateReportTypePolicyService;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result syncReportsForUserAndPlate(Plate plate, Long userId, List<String> reportTypeCodes) {
        if (plate == null || plate.getId() == null || userId == null || reportTypeCodes == null) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_TYPE_INVALID));
        }

        Set<String> normalizedCodes = normalizeCodes(reportTypeCodes);
        if (normalizedCodes == null) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_TYPE_INVALID));
        }

        List<PlateReportType> activeTypes = normalizedCodes.isEmpty()
                ? List.of()
                : plateReportTypeDao.findByCodeInAndActiveTrue(normalizedCodes);

        if (activeTypes.size() != normalizedCodes.size()) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_TYPE_INVALID));
        }

        Map<Long, PlateReportType> desiredTypeById = activeTypes.stream()
                .collect(Collectors.toMap(PlateReportType::getId, Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        List<PlateReport> changes = new ArrayList<>();

        List<PlateReport> existingActive = plateReportDao.findByPlateIdAndUserIdAndActiveTrue(plate.getId(), userId);
        for (PlateReport activeReport : existingActive) {
            if (!desiredTypeById.containsKey(activeReport.getReportType().getId())) {
                activeReport.setActive(false);
                activeReport.setDeactivatedAt(now);
                activeReport.setUpdatedAt(now);
                changes.add(activeReport);
            }
        }

        for (PlateReportType type : activeTypes) {
            PlateReport report = plateReportDao.findByPlateIdAndUserIdAndReportTypeId(
                            plate.getId(),
                            userId,
                            type.getId()
                    )
                    .orElseGet(() -> createNewReport(plate, userId, type, now));

            if (report.getId() != null) {
                if (!report.isActive()) {
                    report.setActive(true);
                    report.setDeactivatedAt(null);
                }
                if (report.getFirstReportedAt() == null) {
                    report.setFirstReportedAt(now);
                }
                report.setLastReportedAt(now);
                report.setUpdatedAt(now);
            }
            changes.add(report);
        }

        if (!changes.isEmpty()) {
            plateReportDao.saveAll(changes);
        }

        return new SuccessResult(messageService.getMessage(Messages.PLATE_REPORTS_SYNCED));
    }

    private PlateReport createNewReport(Plate plate, Long userId, PlateReportType type, LocalDateTime now) {
        PlateReport report = new PlateReport();
        report.setPlate(plate);

        User user = new User();
        user.setId(userId);
        report.setUser(user);

        report.setReportType(type);
        report.setActive(true);
        report.setFirstReportedAt(now);
        report.setLastReportedAt(now);
        report.setDeactivatedAt(null);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        return report;
    }

    private Set<String> normalizeCodes(Collection<String> rawCodes) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String code : rawCodes) {
            if (code == null) {
                return null;
            }
            String normalizedCode = plateReportTypePolicyService.normalizeIncomingCode(code);
            if (normalizedCode.isBlank()) {
                return null;
            }
            normalized.add(normalizedCode);
        }
        return normalized;
    }
}
