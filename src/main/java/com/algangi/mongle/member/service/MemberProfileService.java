package com.algangi.mongle.member.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.domain.oauth2.OAuth2Provider;
import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.presentation.dto.UserDetailResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberFinder memberFinder;
    private final ViewUrlIssueService viewUrlIssueService;

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetails(String memberId) {
        Member member = memberFinder.getMemberOrThrow(memberId);
        String profileImageKey = member.getProfileImage();
        String profileImageUrl = (profileImageKey != null)
            ? viewUrlIssueService.issueViewUrl(profileImageKey).presignedUrl()
            : null;

        Set<OAuth2Provider> linkedProviders = member.getSocialAccounts().stream()
            .map(socialAccount -> socialAccount.getSocialId().getProvider())
            .collect(Collectors.toSet());

        return UserDetailResponse.of(
            member.getNickname(),
            member.getEmail(),
            profileImageUrl
        );
    }

}
