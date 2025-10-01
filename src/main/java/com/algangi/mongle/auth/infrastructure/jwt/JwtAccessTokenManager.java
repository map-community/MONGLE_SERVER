package com.algangi.mongle.auth.infrastructure.jwt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.application.service.AccessTokenManager;
import com.algangi.mongle.auth.domain.model.AccessToken;
import com.algangi.mongle.auth.infrastructure.security.authentication.CustomUserDetails;
import com.algangi.mongle.member.domain.MemberRole;

import io.jsonwebtoken.Claims;

@Service
public class JwtAccessTokenManager implements AccessTokenManager {

    private static final String CLAIM_KEY_ROLE = "role";
    private final JwtHandler jwtHandler;
    private final long accessTokenExpirationMillis;

    public JwtAccessTokenManager(JwtHandler jwtHandler, JwtProperties jwtProperties) {
        this.jwtHandler = jwtHandler;
        this.accessTokenExpirationMillis = jwtProperties.accessTokenExpirationMillis();
    }

    @Override
    public AccessToken generate(String memberId, MemberRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_ROLE, role.name());
        String accessToken = jwtHandler.createToken(memberId, claims,
            accessTokenExpirationMillis);
        return new AccessToken(accessToken, accessTokenExpirationMillis);
    }

    @Override
    public void validateToken(String token) {
        jwtHandler.validateSignature(token);
    }

    @Override
    public Authentication getAuthentication(String accessToken) {
        Claims claims = jwtHandler.parseClaims(accessToken);
        String memberId = claims.getSubject();
        String role = claims.get(CLAIM_KEY_ROLE, String.class);

        CustomUserDetails userDetails = new CustomUserDetails(memberId,
            Collections.singleton(new SimpleGrantedAuthority(role)));

        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

    @Override
    public String getUserId(String token) {
        Claims claims = jwtHandler.parseClaims(token);
        return claims.getSubject();
    }

}
