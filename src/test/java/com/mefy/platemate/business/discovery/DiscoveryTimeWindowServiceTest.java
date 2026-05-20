package com.mefy.platemate.business.discovery;

import com.mefy.platemate.business.discovery.model.DiscoveryTimeWindow;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscoveryTimeWindowServiceTest {

    private static final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

    @Test
    void todayWindowUsesIstanbulBoundaries() {
        DiscoveryTimeWindowService service = new DiscoveryTimeWindowService();

        DiscoveryTimeWindow window = service.todayWindow();

        assertEquals(LocalTime.MIDNIGHT, window.getStart().toLocalTime());
        assertEquals(window.getStart().plusDays(1), window.getEnd());
        assertEquals(LocalDate.now(ISTANBUL), window.getStart().toLocalDate());
    }

    @Test
    void lastDaysWindowReturnsExpectedRange() {
        DiscoveryTimeWindowService service = new DiscoveryTimeWindowService();

        DiscoveryTimeWindow window = service.lastDaysWindow(7);

        assertEquals(Duration.ofDays(7), Duration.between(window.getStart(), window.getEnd()));
    }
}
