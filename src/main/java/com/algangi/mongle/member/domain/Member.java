package com.algangi.mongle.member.domain;

import com.algangi.mongle.auth.domain.OAuthProvider;
import com.algangi.mongle.global.entity.TimeBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long memberId;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String nickname;

    String profileImage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    MemberRole memberRole;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    MemberStatus status = MemberStatus.ACTIVE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Column(nullable = false)
    private String oauthId;

    public static Member createUser(String email, String nickname, String profileImage,
        OAuthProvider provider, String oauthId) {
        return Member.builder()
            .email(email)
            .nickname(nickname)
            .profileImage(profileImage)
            .memberRole(MemberRole.USER)
            .provider(provider)
            .oauthId(oauthId)
            .build();
    }

}
