package com.algangi.mongle.member.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.auth.application.service.OAuth2Service;
import com.algangi.mongle.auth.infrastructure.security.authentication.CustomUserDetails;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.member.presentation.dto.UserDetailResponse;
import com.algangi.mongle.member.service.MemberProfileService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/user/me")
@RequiredArgsConstructor
public class UserController {

    private final OAuth2Service oAuth2Service;
    private final MemberProfileService memberProfileService;

    @PostMapping("/social-link/{registrationId}")
    public ResponseEntity<ApiResponse<Void>> linkSocialAccount(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable(name = "registrationId") String registrationId,
        @RequestParam(name = "code") String authorizationCode
    ) {
        oAuth2Service.linkSocialAccount(userDetails.userId(), registrationId, authorizationCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetails(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(memberProfileService.getUserDetails(userDetails.userId())));
    }

}
