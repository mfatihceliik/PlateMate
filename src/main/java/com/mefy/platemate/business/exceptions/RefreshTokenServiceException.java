package com.mefy.platemate.business.exceptions;

import com.mefy.platemate.config.jwt.JwtTokenProvider;

public class RefreshTokenServiceException extends RuntimeException {
    private final JwtTokenProvider.RefreshTokenErrorCode code;

    public RefreshTokenServiceException(JwtTokenProvider.RefreshTokenErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public JwtTokenProvider.RefreshTokenErrorCode getCode() {
        return code;
    }
}
