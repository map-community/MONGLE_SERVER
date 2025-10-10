package com.algangi.mongle.auth.application.service.authentication;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.application.service.email.VerificationTokenManager;
import com.algangi.mongle.auth.event.MemberSignedUpEvent;
import com.algangi.mongle.auth.presentation.dto.SignUpRequest;
import com.algangi.mongle.auth.presentation.dto.SignUpResponse;
import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.member.application.service.MemberFinder;
import com.algangi.mongle.member.domain.model.Member;
import com.algangi.mongle.member.domain.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final MemberFinder memberFinder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenManager verificationTokenManager;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        verificationTokenManager.validateToken(request.verificationToken(), request.email());

        memberFinder.validateDuplicateEmail(request.email());
        memberFinder.validateDuplicateNickName(request.nickname());

        String profileImageKey = request.profileImageKey();
        if (profileImageKey != null) {
            fileService.validateTemporaryFilesExist(List.of(profileImageKey));
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member newMember = Member.createUser(
            request.email(),
            encodedPassword,
            request.nickname(),
            request.profileImageKey()
        );

        Member savedMember = memberRepository.save(newMember);

        eventPublisher.publishEvent(new MemberSignedUpEvent(
            savedMember.getMemberId(),
            request.profileImageKey()
        ));

        return SignUpResponse.from(savedMember);
    }

}

