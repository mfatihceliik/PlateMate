package com.mefy.platemate.core.utilities.results;

import lombok.Getter;

@Getter
public class Result {
    private String message;
    private boolean success;

    public Result(boolean success) {
        this.success = success;
    }

    public Result(String message, boolean success) {
        this(success);
        this.message = message;
    }

}
