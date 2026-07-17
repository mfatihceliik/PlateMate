package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.IDiscoveryTabOptionDao;
import com.mefy.platemate.entities.concrete.DiscoveryTabOption;
import com.mefy.platemate.entities.dto.DiscoveryTabOptionAdminDto;
import com.mefy.platemate.entities.dto.request.AddDiscoveryTabOptionRequest;
import com.mefy.platemate.entities.dto.request.UpdateDiscoveryTabOptionRequest;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryTabOptionManagerTest {

    @Mock
    private IDiscoveryTabOptionDao discoveryTabOptionDao;
    @Mock
    private LocalizedEnumService localizedEnumService;
    @Mock
    private IMessageService messageService;

    private DiscoveryTabOptionManager manager;

    @BeforeEach
    void setUp() {
        manager = new DiscoveryTabOptionManager(discoveryTabOptionDao, localizedEnumService, messageService);
    }

    @Test
    void addTabOptionReturnsErrorForUnknownCode() {
        AddDiscoveryTabOptionRequest request = new AddDiscoveryTabOptionRequest("MOST_COMMENTED", "Most Commented", 4);
        when(messageService.getMessage(Messages.VALIDATION_DISCOVERY_TAB_OPTION_CODE_INVALID)).thenReturn("invalid-code");

        DataResult<DiscoveryTabOptionAdminDto> result = manager.addTabOption(request);

        assertFalse(result.isSuccess());
        assertEquals("invalid-code", result.getMessage());
    }

    @Test
    void addTabOptionReturnsErrorWhenCodeAlreadyExists() {
        AddDiscoveryTabOptionRequest request = new AddDiscoveryTabOptionRequest("TREND", "Trend", 0);
        when(discoveryTabOptionDao.existsByCode("TREND")).thenReturn(true);
        when(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_ALREADY_EXISTS)).thenReturn("exists");

        DataResult<DiscoveryTabOptionAdminDto> result = manager.addTabOption(request);

        assertFalse(result.isSuccess());
        assertEquals("exists", result.getMessage());
    }

    @Test
    void addTabOptionCreatesRecordWhenValid() {
        AddDiscoveryTabOptionRequest request = new AddDiscoveryTabOptionRequest("new", "Newest", 3);
        DiscoveryTabOption saved = entity(5L, "NEW", "Newest", 3);

        when(discoveryTabOptionDao.existsByCode("NEW")).thenReturn(false);
        when(discoveryTabOptionDao.save(any(DiscoveryTabOption.class))).thenReturn(saved);
        when(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_ADDED)).thenReturn("added");

        DataResult<DiscoveryTabOptionAdminDto> result = manager.addTabOption(request);

        assertTrue(result.isSuccess());
        assertEquals("added", result.getMessage());
        assertEquals("NEW", result.getData().getCode());
    }

    @Test
    void updateTabOptionReturnsErrorForDuplicateCode() {
        DiscoveryTabOption existing = entity(1L, "OLD_CODE", "Old", 5);
        DiscoveryTabOption duplicate = entity(2L, "GOOD_DRIVER", "Good Driver", 2);
        UpdateDiscoveryTabOptionRequest request = new UpdateDiscoveryTabOptionRequest("GOOD_DRIVER", "Good Driver", 2);

        when(discoveryTabOptionDao.findById(1L)).thenReturn(Optional.of(existing));
        when(discoveryTabOptionDao.findByCode("GOOD_DRIVER")).thenReturn(Optional.of(duplicate));
        when(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_ALREADY_EXISTS)).thenReturn("exists");

        DataResult<DiscoveryTabOptionAdminDto> result = manager.updateTabOption(1L, request);

        assertFalse(result.isSuccess());
        assertEquals("exists", result.getMessage());
    }

    @Test
    void setTabOptionActiveReturnsNotFoundWhenMissing() {
        when(discoveryTabOptionDao.findById(99L)).thenReturn(Optional.empty());
        when(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_NOT_FOUND)).thenReturn("not-found");

        Result result = manager.setTabOptionActive(99L, false);

        assertFalse(result.isSuccess());
        assertEquals("not-found", result.getMessage());
    }

    @Test
    void setTabOptionActiveUpdatesState() {
        DiscoveryTabOption existing = entity(4L, "TREND", "Trend", 0);
        existing.setActive(true);

        when(discoveryTabOptionDao.findById(4L)).thenReturn(Optional.of(existing));
        when(messageService.getMessage(Messages.DISCOVERY_TAB_OPTION_STATUS_UPDATED)).thenReturn("updated");

        Result result = manager.setTabOptionActive(4L, false);

        assertTrue(result.isSuccess());
        assertEquals("updated", result.getMessage());
        assertFalse(existing.isActive());
        verify(discoveryTabOptionDao).save(existing);
    }

    private DiscoveryTabOption entity(Long id, String code, String label, int sortOrder) {
        DiscoveryTabOption option = new DiscoveryTabOption();
        option.setId(id);
        option.setCode(code);
        option.setLabel(label);
        option.setSortOrder(sortOrder);
        option.setActive(true);
        option.setCreatedAt(LocalDateTime.now().minusDays(1));
        option.setUpdatedAt(LocalDateTime.now());
        return option;
    }
}
