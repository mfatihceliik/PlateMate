package com.mefy.platemate.core.exceptions;

import lombok.Getter;

@Getter
public class InvalidPaginationException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] args;

    public InvalidPaginationException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }
}
