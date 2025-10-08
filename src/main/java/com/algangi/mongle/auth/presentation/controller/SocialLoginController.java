package com.algangi.mongle.auth.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.auth.application.service.oauth.OAuth2Service;
import com.algangi.mongle.auth.presentation.dto.AuthorizationUrlResponse;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SocialLoginController {

    private final OAuth2Service oauth2Service;

    @GetMapping("social/{registrationId}/authorization-url")
    public AuthorizationUrlResponse getAuthorizationUrl(
        @PathVariable(name = "registrationId") String registrationId) {
        String authorizationUrl = oauth2Service.getAuthorizationUrl(registrationId);
        return AuthorizationUrlResponse.from(authorizationUrl);
    }

    @PostMapping("social/{registrationId}/login")
    public ResponseEntity<ApiResponse<TokenInfo>> socialLogin(
        @PathVariable(name = "registrationId") String registrationId,
        @RequestParam(name = "code") String authorizationCode
    ) {
        TokenInfo tokenInfo = oauth2Service.socialLogin(registrationId, authorizationCode);
        return ResponseEntity.ok(ApiResponse.success(tokenInfo));
    }
}
