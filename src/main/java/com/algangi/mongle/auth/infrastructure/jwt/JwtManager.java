package com.algangi.mongle.auth.infrastructure.jwt;

import java.util.Collections;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.algangi.mongle.auth.application.service.TokenManager;
import com.algangi.mongle.auth.infrastructure.security.CustomUserDetails;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.member.domain.MemberRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtManager implements TokenManager {

    private static final String BEARER_TYPE = "bearer";
    private static final String CLAIM_KEY_ROLE = "role";
    private final SecretKey key;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;
    private final String issuer;

    public JwtManager(JwtProperties jwtProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMillis = jwtProperties.accessTokenExpirationMillis();
        this.refreshTokenExpirationMillis = jwtProperties.refreshTokenExpirationMillis();
        this.issuer = jwtProperties.issuer();
    }

    @Override
    public TokenInfo generateTokens(Long memberId, MemberRole role) {
        String accessToken = generateAccessToken(memberId, role);
        String refreshToken = generateRefreshToken(memberId);

        return TokenInfo.of(BEARER_TYPE, accessToken, refreshToken, accessTokenExpirationMillis,
            refreshTokenExpirationMillis);
    }

    public String generateAccessToken(Long memberId, MemberRole role) {
        Date now = new Date();
        Date accessTokenExpiresIn = new Date(now.getTime() + accessTokenExpirationMillis);

        return Jwts.builder()
            .issuer(issuer)
            .issuedAt(now)
            .subject(memberId.toString())
            .claim(CLAIM_KEY_ROLE, role.name())
            .expiration(accessTokenExpiresIn)
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(Long memberId) {
        Date now = new Date();
        Date refreshTokenExpiresIn = new Date(now.getTime() + refreshTokenExpirationMillis);

        return Jwts.builder()
            .issuer(issuer)
            .issuedAt(now)
            .subject(memberId.toString())
            .expiration(refreshTokenExpiresIn)
            .signWith(key)
            .compact();
    }

    @Override
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Long memberId = Long.parseLong(claims.getSubject());
        String role = claims.get(CLAIM_KEY_ROLE, String.class);

        CustomUserDetails userDetails = new CustomUserDetails(memberId,
            Collections.singleton(new SimpleGrantedAuthority(role)));

        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload(); // getBody() 대신 getPayload() 사용
    }
}