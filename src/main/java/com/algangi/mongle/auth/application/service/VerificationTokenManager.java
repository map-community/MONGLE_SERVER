package com.algangi.mongle.auth.application.service;

public interface VerificationTokenManager {

    String generate(String email);

    void validateToken(String token, String email);

}
