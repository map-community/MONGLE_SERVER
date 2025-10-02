package com.algangi.mongle.auth.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.auth.application.service.LoginService;
import com.algangi.mongle.auth.application.service.TokenReissueService;
import com.algangi.mongle.auth.presentation.dto.LoginRequest;
import com.algangi.mongle.auth.presentation.dto.ReissueTokenRequest;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final TokenReissueService tokenReissueService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenInfo>> loginMember(
        @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loginService.login(request)));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenInfo>> reissueTokens(
        @Valid @RequestBody ReissueTokenRequest reissueTokenRequest) {
        return ResponseEntity.ok(
            ApiResponse.success(tokenReissueService.reissueTokens(reissueTokenRequest)));
    }
}
