package com.mefy.platemate.business.utilities.time;

import java.time.LocalDateTime;

/**
 * Sorgu zaman aralığını temsil eden değer nesnesi.
 * Discovery, notification, statistics gibi tüm domain'lerde kullanılabilir.
 */
public class TimeWindow {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public TimeWindow(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
