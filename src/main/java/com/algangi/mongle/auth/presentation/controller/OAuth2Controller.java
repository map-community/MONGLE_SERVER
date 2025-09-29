package com.algangi.mongle.auth.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.algangi.mongle.auth.application.service.OAuth2Service;
import com.algangi.mongle.auth.infrastructure.security.authentication.CustomUserDetails;
import com.algangi.mongle.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @PostMapping("/login/{registrationId}")
    public RedirectView socialLogin(@PathVariable(name = "registrationId") String registrationId) {
        String authorizationUrl = oauth2Service.getAuthorizationUrl(registrationId);
        return new RedirectView(authorizationUrl);
    }

    @PostMapping("/link/kakao")
    public ResponseEntity<ApiResponse<Void>> linkKakaoAccount(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("code") String authorizationCode
    ) {
        oauth2Service.linkKakaoAccount(userDetails.userId(), authorizationCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
