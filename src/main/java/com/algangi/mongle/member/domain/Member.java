package com.algangi.mongle.member.domain;

import java.util.ArrayList;
import java.util.List;

import com.algangi.mongle.global.entity.TimeBaseEntity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member", uniqueConstraints = {
    @UniqueConstraint(name = "uk_member_email", columnNames = "email"),
    @UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Member extends TimeBaseEntity {

    @Id
    @Tsid
    String memberId;
    @Column(nullable = false, updatable = false)
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
    private String encodedPassword;
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    public static Member createUser(String email, String encodedPassword, String nickname,
        String profileImage) {
        validateUserEssentials(email, encodedPassword, nickname);
        validatePasswordEncoding(encodedPassword);
        return Member.builder()
            .email(email)
            .encodedPassword(encodedPassword)
            .nickname(nickname)
            .profileImage(profileImage)
            .memberRole(MemberRole.USER)
            .build();
    }

    private static void validatePasswordEncoding(String encodedPassword) {
        if (!encodedPassword.startsWith("{bcrypt}") && !encodedPassword.startsWith("$2")) {
            throw new IllegalArgumentException("인코딩되지 않은 비밀번호는 저장할 수 없습니다.");
        }
    }

    private static void validateUserEssentials(String email, String password, String nickname) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수값입니다.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수값입니다.");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수값입니다");
        }
    }

    public void addSocialAccount(SocialAccount socialAccount) {
        socialAccounts.add(socialAccount);
        socialAccount.setMember(this);
    }

    public void ban() {
        this.status = MemberStatus.BANNED;
    }
}
