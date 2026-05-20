package com.mefy.platemate.business.discovery.model;

import com.mefy.platemate.entities.concrete.Plate;

public class ScoredDiscoveryPlate {
    private final Plate plate;
    private final PlateDailyMetrics metrics;
    private final double score;

    public ScoredDiscoveryPlate(Plate plate, PlateDailyMetrics metrics, double score) {
        this.plate = plate;
        this.metrics = metrics;
        this.score = score;
    }

    public Plate getPlate() {
        return plate;
    }

    public PlateDailyMetrics getMetrics() {
        return metrics;
    }

    public double getScore() {
        return score;
    }
}
