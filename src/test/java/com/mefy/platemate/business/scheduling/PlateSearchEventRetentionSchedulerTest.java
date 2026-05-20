package com.mefy.platemate.business.scheduling;

import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlateSearchEventRetentionSchedulerTest {

    @Test
    void cleanupExpiredEventsAtDeletesRowsOlderThan180Days() {
        IPlateSearchEventDao plateSearchEventDao = mock(IPlateSearchEventDao.class);
        PlateSearchEventRetentionScheduler scheduler = new PlateSearchEventRetentionScheduler(plateSearchEventDao);

        LocalDateTime referenceTime = LocalDateTime.of(2026, 5, 18, 3, 30, 0);
        scheduler.cleanupExpiredEventsAt(referenceTime);

        verify(plateSearchEventDao).deleteBySearchedAtBefore(referenceTime.minusDays(180));
    }
}
