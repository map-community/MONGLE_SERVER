package com.algangi.mongle.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.presentation.dto.SignUpRequest;
import com.algangi.mongle.auth.presentation.dto.SignUpResponse;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ApplicationException(AuthErrorCode.DUPLICATE_EMAIL)
                .addErrorInfo("email", request.email());
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new ApplicationException(AuthErrorCode.DUPLICATE_NICKNAME)
                .addErrorInfo("nickname", request.nickname());
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member newMember = Member.createUser(
            request.email(),
            encodedPassword,
            request.nickname(),
            request.profileImageKey()
        );

        Member savedMember = memberRepository.save(newMember);

        return SignUpResponse.from(savedMember);
    }
}

