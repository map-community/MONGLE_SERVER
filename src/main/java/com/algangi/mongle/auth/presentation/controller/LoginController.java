package com.algangi.mongle.auth.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.auth.application.service.LoginService;
import com.algangi.mongle.auth.presentation.dto.LoginRequest;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    public ResponseEntity<ApiResponse<TokenInfo>> loginMember(
        @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loginService.login(request)));
    }
}
