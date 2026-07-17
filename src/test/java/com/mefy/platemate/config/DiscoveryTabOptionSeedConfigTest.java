package com.mefy.platemate.config;

import com.mefy.platemate.dataAccess.abstracts.IDiscoveryTabOptionDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryTabOptionSeedConfigTest {

    @Mock
    private IDiscoveryTabOptionDao discoveryTabOptionDao;

    @Test
    void seedDefaultsOnlyCreatesMissingRecords() throws Exception {
        when(discoveryTabOptionDao.existsByCode(anyString())).thenReturn(false);
        when(discoveryTabOptionDao.existsByCode("TREND")).thenReturn(true);

        DiscoveryTabOptionSeedConfig config = new DiscoveryTabOptionSeedConfig(discoveryTabOptionDao);
        config.seedDiscoveryTabOptions().run(null);

        verify(discoveryTabOptionDao, never()).save(org.mockito.ArgumentMatchers.argThat(option -> "TREND".equals(option.getCode())));
        verify(discoveryTabOptionDao, times(3)).save(any());
    }
}
