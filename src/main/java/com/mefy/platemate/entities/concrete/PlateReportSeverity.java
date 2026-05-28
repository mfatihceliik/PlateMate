package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum PlateReportSeverity {
    RED(1L, "RED"),
    YELLOW(2L, "YELLOW");

    private final Long id;
    private final String code;

    PlateReportSeverity(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static PlateReportSeverity fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static PlateReportSeverity fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static PlateReportSeverity resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
