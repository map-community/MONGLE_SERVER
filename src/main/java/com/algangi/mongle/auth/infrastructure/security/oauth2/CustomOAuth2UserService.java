package com.algangi.mongle.auth.infrastructure.security.oauth2;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.infrastructure.oauth2.OAuth2UserInfo;
import com.algangi.mongle.auth.infrastructure.oauth2.OAuth2UserInfoFactory;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.domain.SocialAccount;
import com.algangi.mongle.member.repository.SocialAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountRepository socialAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.createOAuth2UserInfo(registrationId,
            oAuth2User.getAttributes());

        String provider = userInfo.getProvider();
        String providerId = userInfo.getProviderId();

        SocialAccount socialAccount = socialAccountRepository.findByProviderAndProviderId(provider,
                providerId)
            .orElseThrow(() -> new ApplicationException(AuthErrorCode.NOT_LINKED_ACCOUNT));

        Member member = socialAccount.getMember();

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(member.getMemberRole().getRole())),
            Map.of(
                "memberId", member.getMemberId(),
                "role", member.getMemberRole()
            ),
            "memberId"
        );
    }
}
