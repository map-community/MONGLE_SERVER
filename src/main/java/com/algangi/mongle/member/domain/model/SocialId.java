package com.algangi.mongle.member.domain.model;

import com.algangi.mongle.auth.domain.oauth2.OAuth2Provider;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class SocialId implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuth2Provider provider;

    @Column(nullable = false)
    private String providerId;

    public static SocialId of(OAuth2Provider oAuth2Provider, String providerId) {
        return new SocialId(oAuth2Provider, providerId);
    }
}