package com.algangi.mongle.auth.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.auth.application.SignUpService;
import com.algangi.mongle.auth.presentation.dto.SignUpRequest;
import com.algangi.mongle.auth.presentation.dto.SignUpResponse;
import com.algangi.mongle.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sign-up")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpService signUpService;

    @PostMapping
    public ResponseEntity<ApiResponse<SignUpResponse>> signupMember(
        @Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(signUpService.signUp(request)));
    }

}
