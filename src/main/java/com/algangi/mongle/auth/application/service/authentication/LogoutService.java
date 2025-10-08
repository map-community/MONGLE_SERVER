package com.algangi.mongle.auth.application.service.authentication;

import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.domain.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void logout(String userId) {
        refreshTokenRepository.deleteById(userId);
    }

}
