package com.algangi.mongle.auth.application.service;

import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.presentation.dto.ReissueTokenRequest;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenReissueService {

    private final AuthTokenManager authTokenManager;

    public TokenInfo reissueTokens(ReissueTokenRequest reissueTokenRequest) {
        String refreshToken = reissueTokenRequest.refreshToken();
        return authTokenManager.reissueTokens(refreshToken);
    }

}
