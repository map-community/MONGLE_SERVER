package com.algangi.mongle.auth.application.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.service.MemberFinder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final MemberFinder memberFinder;
    private final MailSender mailSender;
    private final VerificationCodeManager verificationCodeManager;

    public void sendVerificationCode(String email) {
        memberFinder.validateDuplicateEmail(email);

        String code = generateRandomCode();
        verificationCodeManager.save(email, code);
        mailSender.send(email, "몽글(Mongle) 서비스 회원가입 인증 코드입니다.", "인증 코드는 [" + code + "] 입니다.");
    }

    public void verifyEmail(String email, String verificationCode) {
        if (!StringUtils.hasText(verificationCode)) {
            throw new IllegalArgumentException("이메일 인증코드는 빈 값일 수 없습니다.");
        }

        String savedCode = verificationCodeManager.getCode(email);
        if (savedCode == null) {
            throw new ApplicationException(AuthErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!savedCode.equals(verificationCode)) {
            throw new ApplicationException(AuthErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        verificationCodeManager.deleteCode(email);
    }

    private String generateRandomCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
