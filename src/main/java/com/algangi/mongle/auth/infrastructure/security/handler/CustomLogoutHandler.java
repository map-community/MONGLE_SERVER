package com.algangi.mongle.auth.infrastructure.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.algangi.mongle.auth.application.service.LogoutService;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.infrastructure.security.CustomUserDetails;
import com.algangi.mongle.global.exception.ApplicationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final LogoutService logoutService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {
        if (authentication == null) {
            throw new ApplicationException(AuthErrorCode.UNAUTHORIZED);
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.userId();

        logoutService.logout(userId);
    }
}
