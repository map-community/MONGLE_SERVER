package com.algangi.mongle.auth.domain.repository;

import org.springframework.data.repository.CrudRepository;

import com.algangi.mongle.auth.domain.model.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    boolean existsByRefreshToken(String refreshToken);
}
