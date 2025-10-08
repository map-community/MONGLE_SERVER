package com.algangi.mongle.auth.application.service.authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.application.service.email.VerificationTokenManager;
import com.algangi.mongle.auth.presentation.dto.SignUpRequest;
import com.algangi.mongle.auth.presentation.dto.SignUpResponse;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.repository.MemberRepository;
import com.algangi.mongle.member.service.MemberFinder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final MemberFinder memberFinder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenManager verificationTokenManager;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        verificationTokenManager.validateToken(request.verificationToken(), request.email());

        memberFinder.validateDuplicateEmail(request.email());
        memberFinder.validateDuplicateNickName(request.nickname());

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

