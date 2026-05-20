package com.mefy.platemate.business.scheduling;

import com.mefy.platemate.business.utilities.time.TimeWindowService;
import com.mefy.platemate.dataAccess.abstracts.IPlateSearchEventDao;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PlateSearchEventRetentionScheduler {

    static final int RETENTION_DAYS = 180;

    private final IPlateSearchEventDao plateSearchEventDao;

    @Scheduled(
            cron = "${platemate.plate-search-events.retention-cron:0 30 3 * * *}",
            zone = "Europe/Istanbul"
    )
    @Transactional
    public void cleanupExpiredEvents() {
        cleanupExpiredEventsAt(LocalDateTime.now(TimeWindowService.TURKEY_ZONE));
    }

    void cleanupExpiredEventsAt(LocalDateTime referenceTime) {
        LocalDateTime cutoff = referenceTime.minusDays(RETENTION_DAYS);
        plateSearchEventDao.deleteBySearchedAtBefore(cutoff);
    }
}
