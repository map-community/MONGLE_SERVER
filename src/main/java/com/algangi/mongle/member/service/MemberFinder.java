package com.algangi.mongle.member.service;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.exception.MemberErrorCode;
import com.algangi.mongle.member.repository.MemberJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.global.exception.ApplicationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class MemberFinder {

    private final MemberJpaRepository memberJpaRepository;

    public Member getMemberOrThrow(Long memberId) {
        return memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

}
