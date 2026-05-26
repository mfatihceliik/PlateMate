package com.mefy.platemate.config;

import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeDao;
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
class PlateReportTypeSeedConfigTest {

    @Mock
    private IPlateReportTypeDao plateReportTypeDao;

    @Test
    void seedDefaultsOnlyCreatesMissingRecords() throws Exception {
        when(plateReportTypeDao.existsByCode(anyString())).thenReturn(false);
        when(plateReportTypeDao.existsByCode("TRAFFIC_RULE_VIOLATION")).thenReturn(true);

        PlateReportTypeSeedConfig config = new PlateReportTypeSeedConfig(plateReportTypeDao);
        config.seedPlateReportTypes().run(null);

        verify(plateReportTypeDao, never()).save(org.mockito.ArgumentMatchers.argThat(type -> "TRAFFIC_RULE_VIOLATION".equals(type.getCode())));
        verify(plateReportTypeDao, times(13)).save(any());
    }
}
