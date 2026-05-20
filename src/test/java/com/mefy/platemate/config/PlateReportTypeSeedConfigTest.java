package com.mefy.platemate.config;

import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
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
        when(plateReportTypeDao.existsByCode("HIT_AND_RUN")).thenReturn(true);
        when(plateReportTypeDao.existsByCode("AGGRESSIVE_DRIVER")).thenReturn(false);
        when(plateReportTypeDao.existsByCode("RED_LIGHT_VIOLATION")).thenReturn(false);
        when(plateReportTypeDao.existsByCode("WRONG_WAY")).thenReturn(false);
        when(plateReportTypeDao.existsByCode("DRUNK_DRIVING")).thenReturn(false);
        when(plateReportTypeDao.existsByCode("PHONE_USAGE")).thenReturn(false);
        when(plateReportTypeDao.existsByCode("SPEEDING")).thenReturn(false);
        when(plateReportTypeDao.existsByCode("ILLEGAL_PARKING")).thenReturn(false);

        PlateReportTypeSeedConfig config = new PlateReportTypeSeedConfig(plateReportTypeDao);
        config.seedPlateReportTypes().run(null);

        verify(plateReportTypeDao, never()).save(org.mockito.ArgumentMatchers.argThat(type -> "HIT_AND_RUN".equals(type.getCode())));
        verify(plateReportTypeDao, times(7)).save(any());
    }
}
