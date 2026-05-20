package com.mefy.platemate.business.discovery.model;

import java.time.LocalDateTime;

public class PlateDailyMetrics {
    private long todaySearchCount;
    private long todayReviewCount;
    private long todayReportCount;
    private long todayWeightedReportScore;
    private LocalDateTime lastActivityAt;

    public long getTodaySearchCount() {
        return todaySearchCount;
    }

    public void addTodaySearchCount(long value) {
        this.todaySearchCount += value;
    }

    public long getTodayReviewCount() {
        return todayReviewCount;
    }

    public void addTodayReviewCount(long value) {
        this.todayReviewCount += value;
    }

    public long getTodayReportCount() {
        return todayReportCount;
    }

    public void addTodayReportCount(long value) {
        this.todayReportCount += value;
    }

    public long getTodayWeightedReportScore() {
        return todayWeightedReportScore;
    }

    public void addTodayWeightedReportScore(long value) {
        this.todayWeightedReportScore += value;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void mergeLastActivityAt(LocalDateTime candidate) {
        if (candidate == null) {
            return;
        }
        if (lastActivityAt == null || candidate.isAfter(lastActivityAt)) {
            lastActivityAt = candidate;
        }
    }
}
