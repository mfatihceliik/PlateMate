package com.mefy.platemate.business.utilities;

/** Null-safe primitive coercions shared across managers and discovery helpers. */
public final class Numbers {

    private Numbers() {
    }

    public static long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    public static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    public static double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    /** Clamps a {@code long} into {@code int} range (used for review/report counts). */
    public static int toSafeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
