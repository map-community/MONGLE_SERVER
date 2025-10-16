package com.algangi.mongle.auth.application.service.email;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.presentation.dto.VerifyEmailRequest;
import com.algangi.mongle.auth.presentation.dto.VerifyEmailResponse;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.application.service.MemberFinder;

import java.time.Duration;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String RATE_LIMIT_KEY_PREFIX = "email-verification:rate-limit:";
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final int MAX_ATTEMPTS = 3;
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberFinder memberFinder;
    private final MailSender mailSender;
    private final EmailVerificationCodeManager emailVerificationCodeManager;
    private final VerificationTokenManager verificationTokenManager;

    public void sendVerificationCode(String email) {
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(rateLimitKey);

        if (attempts == null) {
            throw new IllegalStateException(
                "Redis increment operation failed for key: " + rateLimitKey);
        }

        if (attempts == 1) {
            redisTemplate.expire(rateLimitKey, RATE_LIMIT_WINDOW);
        }

        if (attempts > MAX_ATTEMPTS) {
            throw new ApplicationException(AuthErrorCode.VERIFICATION_CODE_TRY_EXCEEDED);
        }

        memberFinder.validateDuplicateEmail(email);

        String code = generateRandomCode();
        emailVerificationCodeManager.save(email, code);
        mailSender.send(email, "몽글(Mongle) 서비스 회원가입 인증 코드입니다.", "인증 코드는 [" + code + "] 입니다.");
    }

    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.email();
        String verificationCode = request.verificationCode();

        if (!StringUtils.hasText(verificationCode)) {
            throw new IllegalArgumentException("이메일 인증코드는 빈 값일 수 없습니다.");
        }

        String savedCode = emailVerificationCodeManager.getCode(email);
        if (savedCode == null) {
            throw new ApplicationException(AuthErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!savedCode.equals(verificationCode)) {
            throw new ApplicationException(AuthErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        emailVerificationCodeManager.deleteCode(email);

        String verificationToken = verificationTokenManager.generate(email);
        return new VerifyEmailResponse(verificationToken);
    }

    private String generateRandomCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }
}
