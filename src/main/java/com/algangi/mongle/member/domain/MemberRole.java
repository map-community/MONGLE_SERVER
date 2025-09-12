package com.algangi.mongle.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    USER("ROLE_USER"), ADMIN("ROLE_ADMIN");

    private final String role;
}

