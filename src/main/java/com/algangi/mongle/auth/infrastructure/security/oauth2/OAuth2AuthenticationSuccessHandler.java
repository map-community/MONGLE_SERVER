package com.algangi.mongle.auth.infrastructure.security.oauth2;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.algangi.mongle.auth.application.service.AuthTokenManager;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.member.domain.MemberRole;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthTokenManager authTokenManager;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Long memberId = oAuth2User.getAttribute("memberId");
        MemberRole role = oAuth2User.getAttribute("role");

        TokenInfo tokenInfo = authTokenManager.generateTokens(memberId, role);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResponse<TokenInfo> apiResponse = ApiResponse.success(tokenInfo);
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
