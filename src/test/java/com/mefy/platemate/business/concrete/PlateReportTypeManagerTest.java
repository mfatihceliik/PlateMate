package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.concrete.PlateReportTypePolicyService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeTranslationDao;
import com.mefy.platemate.entities.concrete.PlateReportSeverity;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.dto.PlateReportTypeAdminDto;
import com.mefy.platemate.entities.dto.request.AddPlateReportTypeRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateReportTypeManagerTest {

    @Mock
    private IPlateReportDao plateReportDao;
    @Mock
    private IPlateReportTypeDao plateReportTypeDao;
    @Mock
    private IPlateReportTypeTranslationDao translationDao;
    @Mock
    private IMessageService messageService;

    private PlateReportTypeManager manager;

    @BeforeEach
    void setUp() {
        manager = new PlateReportTypeManager(
                plateReportDao,
                plateReportTypeDao,
                translationDao,
                new PlateReportTypePolicyService(),
                messageService
        );
    }

    @Test
    void addReportTypeReturnsErrorWhenCodeAlreadyExists() {
        AddPlateReportTypeRequest request = request("TRAFFIC_RULE_VIOLATION");

        when(plateReportTypeDao.existsByCode("TRAFFIC_RULE_VIOLATION")).thenReturn(true);
        when(messageService.getMessage(Messages.REPORT_TYPE_ALREADY_EXISTS)).thenReturn("exists");

        DataResult<PlateReportTypeAdminDto> result = manager.addReportType(request);

        assertFalse(result.isSuccess());
        assertEquals("exists", result.getMessage());
    }

    @Test
    void addReportTypeCreatesRecordWhenCodeUnique() {
        AddPlateReportTypeRequest request = request("tailgating");
        PlateReportType saved = entity(11L, "TAILGATING");

        when(plateReportTypeDao.existsByCode("TAILGATING")).thenReturn(false);
        when(plateReportTypeDao.save(any(PlateReportType.class))).thenReturn(saved);
        when(messageService.getMessage(Messages.PLATE_REPORT_TYPE_ADDED)).thenReturn("added");

        DataResult<PlateReportTypeAdminDto> result = manager.addReportType(request);

        assertTrue(result.isSuccess());
        assertEquals("added", result.getMessage());
        assertEquals("TAILGATING", result.getData().getCode());
    }

    @Test
    void updateReportTypeReturnsErrorForDuplicateCode() {
        PlateReportType existing = entity(1L, "OLD_CODE");
        PlateReportType duplicate = entity(2L, "TRAFFIC_RULE_VIOLATION");
        UpdatePlateReportTypeRequest request = updateRequest("TRAFFIC_RULE_VIOLATION");

        when(plateReportTypeDao.findById(1L)).thenReturn(Optional.of(existing));
        when(plateReportTypeDao.findByCode("TRAFFIC_RULE_VIOLATION")).thenReturn(Optional.of(duplicate));
        when(messageService.getMessage(Messages.REPORT_TYPE_ALREADY_EXISTS)).thenReturn("exists");

        DataResult<PlateReportTypeAdminDto> result = manager.updateReportType(1L, request);

        assertFalse(result.isSuccess());
        assertEquals("exists", result.getMessage());
    }

    @Test
    void setReportTypeActiveReturnsNotFoundWhenMissing() {
        when(plateReportTypeDao.findById(99L)).thenReturn(Optional.empty());
        when(messageService.getMessage(Messages.REPORT_TYPE_NOT_FOUND)).thenReturn("not-found");

        Result result = manager.setReportTypeActive(99L, false);

        assertFalse(result.isSuccess());
        assertEquals("not-found", result.getMessage());
    }

    @Test
    void setReportTypeActiveUpdatesStateWhenFound() {
        PlateReportType existing = entity(4L, "SPEEDING");
        existing.setActive(true);

        when(plateReportTypeDao.findById(4L)).thenReturn(Optional.of(existing));
        when(messageService.getMessage(Messages.PLATE_REPORT_TYPE_STATUS_UPDATED)).thenReturn("updated");

        Result result = manager.setReportTypeActive(4L, false);

        assertTrue(result.isSuccess());
        assertEquals("updated", result.getMessage());
        assertFalse(existing.isActive());
        verify(plateReportTypeDao).save(existing);
        verify(plateReportDao).deactivateActiveReportsByTypeId(eq(4L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    private AddPlateReportTypeRequest request(String code) {
        return new AddPlateReportTypeRequest(
                code,
                "Label",
                "Description",
                "icon_key",
                PlateReportSeverity.YELLOW,
                "#F9A825",
                3,
                1
        );
    }

    private UpdatePlateReportTypeRequest updateRequest(String code) {
        return new UpdatePlateReportTypeRequest(
                code,
                "Label 2",
                "Description 2",
                "icon_2",
                PlateReportSeverity.RED,
                "#E53935",
                5,
                2
        );
    }

    private PlateReportType entity(Long id, String code) {
        PlateReportType type = new PlateReportType();
        type.setId(id);
        type.setCode(code);
        type.setLabel("label");
        type.setDescription("desc");
        type.setIconKey("icon");
        type.setSeverity(PlateReportSeverity.RED);
        type.setColorHex("#E53935");
        type.setWeight(5);
        type.setSortOrder(1);
        type.setActive(true);
        type.setCreatedAt(LocalDateTime.now().minusDays(1));
        type.setUpdatedAt(LocalDateTime.now());
        return type;
    }
}
