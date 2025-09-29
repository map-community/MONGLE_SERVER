package com.algangi.mongle.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.algangi.mongle.auth.domain.oauth2.OAuth2Provider;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.infrastructure.kakao.KakaoApiClient;
import com.algangi.mongle.auth.infrastructure.kakao.KakaoUserInfoResponse;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.domain.SocialAccount;
import com.algangi.mongle.member.domain.SocialId;
import com.algangi.mongle.member.exception.MemberErrorCode;
import com.algangi.mongle.member.repository.MemberRepository;
import com.algangi.mongle.member.repository.SocialAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final KakaoApiClient kakaoApiClient;
    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Transactional
    public void linkKakaoAccount(Long memberId, String authorizationCode) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        String accessToken = kakaoApiClient.getAccessToken(authorizationCode);
        KakaoUserInfoResponse userInfo = kakaoApiClient.getUserInfo(accessToken);
        SocialId socialId = SocialId.of(OAuth2Provider.KAKAO, userInfo.providerId());

        socialAccountRepository.findBySocialId(socialId)
            .ifPresent(account -> {
                throw new ApplicationException(AuthErrorCode.DUPLICATE_SOCIAL_ACCOUNT);
            });

        SocialAccount socialAccount = SocialAccount.from(socialId);
        member.addSocialAccount(socialAccount);
    }

    public String getAuthorizationUrl(String registrationId) {
        return UriComponentsBuilder
            .fromPath("/oauth2/authorization/{registrationId}")
            .buildAndExpand(registrationId)
            .toUriString();
    }

}
