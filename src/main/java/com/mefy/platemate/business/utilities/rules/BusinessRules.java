package com.mefy.platemate.business.utilities.rules;


import com.mefy.platemate.core.utilities.results.Result;

import java.util.function.Supplier;

public class BusinessRules {

    /**
     * Eager guard chain: every {@code Result} is already computed by the caller before this runs.
     * Returns the first failing result, or {@code null} when all pass.
     */
    public static Result run(Result... logics) {
        for (Result logic : logics) {
            if (!logic.isSuccess())
                return logic;
        }
        return null;
    }

    /**
     * Lazy guard chain: each supplier is evaluated in order and evaluation stops at the first
     * failure, so later (potentially expensive / DB-hitting) guards are skipped once one fails.
     * Returns the first failing result, or {@code null} when all pass.
     */
    @SafeVarargs
    public static Result run(Supplier<Result>... logics) {
        for (Supplier<Result> logic : logics) {
            Result result = logic.get();
            if (!result.isSuccess())
                return result;
        }
        return null;
    }
}
