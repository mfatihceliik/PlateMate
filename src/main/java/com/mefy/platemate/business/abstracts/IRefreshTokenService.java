package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.config.jwt.JwtTokenProvider;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.AuthTokensDto;

public interface IRefreshTokenService {

    AuthTokensDto issueTokens(User user);

    AuthTokensDto refreshTokens(String refreshToken);

    void revoke(String refreshToken);


}
