package com.algangi.mongle.auth.infrastructure.jwt;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtHandler {

    private final SecretKey key;
    private final String issuer;

    public JwtHandler(JwtProperties jwtProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = jwtProperties.issuer();
    }

    public String createToken(String subject, Map<String, Object> claims, long expirationMillis) {
        Date now = new Date();
        Date expiresIn = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
            .issuer(issuer)
            .issuedAt(now)
            .subject(subject)
            .claims(claims) // claims를 파라미터로 받음
            .expiration(expiresIn)
            .signWith(key)
            .compact();
    }

    public void validateSignature(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(AuthErrorCode.EXPIRED_TOKEN, e);
        } catch (Exception e) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN, e);
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

}