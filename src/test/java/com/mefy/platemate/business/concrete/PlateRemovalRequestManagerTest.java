package com.mefy.platemate.business.concrete;

import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateRemovalRequestDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateRemovalRequest;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestReason;
import com.mefy.platemate.entities.concrete.PlateStatus;
import com.mefy.platemate.entities.dto.PlateRemovalRequestDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateRemovalRequestManagerTest {

    @Mock
    private IPlateRemovalRequestDao plateRemovalRequestDao;
    @Mock
    private IPlateDao plateDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private IMessageService messageService;

    private PlateRemovalRequestManager manager;

    @BeforeEach
    void setUp() {
        manager = new PlateRemovalRequestManager(
                plateRemovalRequestDao,
                plateDao,
                userDao,
                messageService
        );
        ReflectionTestUtils.setField(manager, "hidePlateOnRemovalRequest", true);
    }

    @Test
    void addRequestAutoHidesActivePlate() {
        Plate plate = new Plate();
        plate.setId(30L);
        plate.setPlateCode("34ABC123");
        plate.setStatus(PlateStatus.ACTIVE);
        plate.setCreatedAt(LocalDateTime.now());
        plate.setUpdatedAt(LocalDateTime.now());

        PlateRemovalRequest saved = new PlateRemovalRequest();
        saved.setId(99L);
        saved.setPlate(plate);
        saved.setReason(PlateRemovalRequestReason.PRIVACY_REQUEST);
        saved.setDescription("privacy");
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(plateDao.findById(30L)).thenReturn(Optional.of(plate));
        when(userDao.findByIdAndActiveTrue(8L)).thenReturn(Optional.of(new com.mefy.platemate.entities.concrete.User()));
        when(plateRemovalRequestDao.save(any(PlateRemovalRequest.class))).thenReturn(saved);
        when(messageService.getMessage("plate.removal.request.created")).thenReturn("created");

        DataResult<PlateRemovalRequestDto> result = manager.addRequest(
                30L,
                8L,
                new AddPlateRemovalRequestRequest(
                        PlateRemovalRequestReason.PRIVACY_REQUEST,
                        "privacy request",
                        "owner@example.com"
                )
        );

        assertTrue(result.isSuccess());
        assertEquals("created", result.getMessage());

        ArgumentCaptor<Plate> plateCaptor = ArgumentCaptor.forClass(Plate.class);
        verify(plateDao).save(plateCaptor.capture());
        assertEquals(PlateStatus.HIDDEN_BY_REQUEST, plateCaptor.getValue().getStatus());
        assertEquals("AUTO_HIDE_BY_REMOVAL_REQUEST:99", plateCaptor.getValue().getHiddenReason());
    }
}
