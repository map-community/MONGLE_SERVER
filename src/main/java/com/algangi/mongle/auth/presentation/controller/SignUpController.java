package com.algangi.mongle.auth.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.auth.application.service.EmailVerificationService;
import com.algangi.mongle.auth.application.service.SignUpService;
import com.algangi.mongle.auth.presentation.dto.SendVerificationCodeRequest;
import com.algangi.mongle.auth.presentation.dto.SignUpRequest;
import com.algangi.mongle.auth.presentation.dto.SignUpResponse;
import com.algangi.mongle.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpService signUpService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<SignUpResponse>> signupMember(
        @Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(signUpService.signUp(request)));
    }

    @PostMapping("/verification-code")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
        @Valid @RequestBody SendVerificationCodeRequest request) {
        emailVerificationService.sendVerificationCode(request.email());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
