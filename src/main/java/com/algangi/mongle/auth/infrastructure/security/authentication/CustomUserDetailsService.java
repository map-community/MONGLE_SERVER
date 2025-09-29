package com.algangi.mongle.auth.infrastructure.security.authentication;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberFinder memberFinder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberFinder.getMemberOrThrow(Long.parseLong(username));

        return new CustomUserDetails(member.getMemberId(),
            Collections.singleton(new SimpleGrantedAuthority(member.getMemberRole().getRole())));
    }
}
