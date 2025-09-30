package com.algangi.mongle.auth.infrastructure.security.authentication;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public record CustomUserDetails(
    String userId,
    Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public static CustomUserDetails of(String userId,
        Collection<? extends GrantedAuthority> authorities) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("memberId는 null일 수 없습니다.");
        }
        if (authorities == null) {
            authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_GUEST"));
        }
        return new CustomUserDetails(userId, authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

}