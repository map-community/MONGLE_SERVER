package com.algangi.mongle.auth.application.service.email;

public interface VerificationCodeManager {

    void save(String email, String code);

    String getCode(String email);

    void deleteCode(String email);

}
