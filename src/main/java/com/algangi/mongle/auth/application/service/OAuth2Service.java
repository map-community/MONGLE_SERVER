package com.algangi.mongle.auth.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.algangi.mongle.auth.domain.oauth2.OAuth2Provider;
import com.algangi.mongle.auth.domain.oauth2.OAuth2UserInfo;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.domain.SocialAccount;
import com.algangi.mongle.member.domain.SocialId;
import com.algangi.mongle.member.exception.MemberErrorCode;
import com.algangi.mongle.member.repository.MemberRepository;
import com.algangi.mongle.member.repository.SocialAccountRepository;


@Service
public class OAuth2Service {

    private final Map<OAuth2Provider, OAuth2Client> clients;
    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final AuthTokenManager authTokenManager;

    public OAuth2Service(List<OAuth2Client> clientList, MemberRepository memberRepository,
        SocialAccountRepository socialAccountRepository, AuthTokenManager authTokenManager) {
        this.clients = clientList.stream()
            .collect(Collectors.toUnmodifiableMap(OAuth2Client::getProvider, client -> client));
        this.memberRepository = memberRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.authTokenManager = authTokenManager;
    }

    public TokenInfo socialLogin(String registrationId, String authorizationCode) {
        OAuth2Provider provider = OAuth2Provider.from(registrationId);
        OAuth2Client client = clients.get(provider);

        OAuth2UserInfo userProfile = client.fetchUserInfoWithAuthorizationCode(
            authorizationCode);

        SocialAccount socialAccount = socialAccountRepository.findBySocialId(
                SocialId.of(provider, userProfile.providerId()))
            .orElseThrow(() -> new ApplicationException(AuthErrorCode.NOT_LINKED_ACCOUNT));

        Member member = socialAccount.getMember();
        return authTokenManager.generateTokens(member.getMemberId(), member.getMemberRole());
    }

    @Transactional
    public void linkSocialAccount(String memberId, String registrationId,
        String authorizationCode) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));

        OAuth2Provider provider = OAuth2Provider.from(registrationId);
        OAuth2Client client = clients.get(provider);

        OAuth2UserInfo userInfo = client.fetchUserInfoWithAuthorizationCode(
            authorizationCode);
        //현재 providerId를 제외한 email, profile_image, nickname은 사용 안함
        SocialId socialId = SocialId.of(provider, userInfo.providerId());

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
