package com.mefy.platemate.business.utilities.time;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Uygulama genelinde kullanılabilen zaman penceresi üretici servis.
 * Türkiye saatine (Europe/Istanbul) göre çalışır.
 * Discovery, bildirim, istatistik gibi farklı domainlerde yeniden kullanılabilir.
 */
@Component
public class TimeWindowService {

    public static final ZoneId TURKEY_ZONE = ZoneId.of("Europe/Istanbul");

    /**
     * Bugünün zaman penceresini döner: 00:00:00 → yarın 00:00:00
     * (Türkiye saatiyle bugün boyunca gerçekleşen tüm olayları kapsar.)
     */
    public TimeWindow todayWindow() {
        LocalDate today = LocalDate.now(TURKEY_ZONE);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return new TimeWindow(start, end);
    }

    /**
     * Son N günün zaman penceresini döner.
     *
     * @param dayCount geriye gidilecek gün sayısı
     */
    public TimeWindow lastDaysWindow(int dayCount) {
        LocalDate today = LocalDate.now(TURKEY_ZONE);
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        LocalDateTime start = end.minusDays(dayCount);
        return new TimeWindow(start, end);
    }
}
