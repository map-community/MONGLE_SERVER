package com.algangi.mongle.auth.application.service.email;

public interface VerificationTokenManager {

    String generate(String email);

    void validateToken(String token, String email);

}
