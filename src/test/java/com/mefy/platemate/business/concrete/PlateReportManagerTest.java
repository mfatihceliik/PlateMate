package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.plate.concrete.PlateReportTypePolicyService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import com.mefy.platemate.entities.concrete.PlateReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateReportManagerTest {

    @Mock
    private IPlateReportDao plateReportDao;
    @Mock
    private IPlateReportTypeDao plateReportTypeDao;
    @Mock
    private IMessageService messageService;

    private PlateReportManager plateReportManager;

    @BeforeEach
    void setUp() {
        plateReportManager = new PlateReportManager(
                plateReportDao,
                plateReportTypeDao,
                new PlateReportTypePolicyService(),
                messageService
        );
    }

    @Test
    void syncReportsCreatesAndDeactivates() {
        Plate plate = new Plate();
        plate.setId(10L);

        PlateReportType desiredType = buildType(1L, "RED_LIGHT_VIOLATION");
        PlateReport activeToDeactivate = new PlateReport();
        activeToDeactivate.setId(100L);
        activeToDeactivate.setActive(true);
        activeToDeactivate.setReportType(buildType(2L, "PHONE_USAGE"));

        when(plateReportTypeDao.findByCodeInAndActiveTrue(any())).thenReturn(List.of(desiredType));
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(10L, 7L)).thenReturn(List.of(activeToDeactivate));
        when(plateReportDao.findByPlateIdAndUserIdAndReportTypeId(10L, 7L, 1L)).thenReturn(Optional.empty());
        when(messageService.getMessage(Messages.PLATE_REPORTS_SYNCED)).thenReturn("synced");

        Result result = plateReportManager.syncReportsForUserAndPlate(plate, 7L, List.of("red_light_violation"));

        assertTrue(result.isSuccess());
        assertEquals("synced", result.getMessage());

        ArgumentCaptor<List<PlateReport>> captor = ArgumentCaptor.forClass(List.class);
        verify(plateReportDao).saveAll(captor.capture());
        List<PlateReport> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertFalse(saved.get(0).isActive());
        assertTrue(saved.get(1).isActive());
    }

    @Test
    void syncReportsReactivatesExistingInactiveRecord() {
        Plate plate = new Plate();
        plate.setId(10L);

        PlateReportType type = buildType(1L, "WRONG_WAY");
        PlateReport existingInactive = new PlateReport();
        existingInactive.setId(77L);
        existingInactive.setActive(false);
        existingInactive.setReportType(type);
        existingInactive.setFirstReportedAt(LocalDateTime.now().minusDays(2));
        existingInactive.setDeactivatedAt(LocalDateTime.now().minusDays(1));

        when(plateReportTypeDao.findByCodeInAndActiveTrue(any())).thenReturn(List.of(type));
        when(plateReportDao.findByPlateIdAndUserIdAndActiveTrue(10L, 8L)).thenReturn(List.of());
        when(plateReportDao.findByPlateIdAndUserIdAndReportTypeId(10L, 8L, 1L)).thenReturn(Optional.of(existingInactive));
        when(messageService.getMessage(Messages.PLATE_REPORTS_SYNCED)).thenReturn("synced");

        Result result = plateReportManager.syncReportsForUserAndPlate(plate, 8L, List.of("wrong_way"));

        assertTrue(result.isSuccess());
        verify(plateReportDao).saveAll(any());
        assertTrue(existingInactive.isActive());
        assertEquals(null, existingInactive.getDeactivatedAt());
    }

    @Test
    void syncReportsReturnsErrorForInvalidCodes() {
        Plate plate = new Plate();
        plate.setId(10L);

        when(plateReportTypeDao.findByCodeInAndActiveTrue(any())).thenReturn(List.of());
        when(messageService.getMessage(Messages.REPORT_TYPE_INVALID)).thenReturn("invalid");

        Result result = plateReportManager.syncReportsForUserAndPlate(plate, 9L, List.of("NOT_EXISTS"));

        assertFalse(result.isSuccess());
        assertEquals("invalid", result.getMessage());
    }

    private PlateReportType buildType(Long id, String code) {
        PlateReportType type = new PlateReportType();
        type.setId(id);
        type.setCode(code);
        type.setLabel(code);
        type.setDescription(code);
        type.setIconKey(code);
        type.setSeverity(PlateReportSeverity.RED);
        type.setColorHex("#E53935");
        type.setWeight(5);
        type.setSortOrder(1);
        type.setActive(true);
        return type;
    }
}
