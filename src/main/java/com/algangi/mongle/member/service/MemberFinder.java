package com.algangi.mongle.member.service;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.exception.MemberErrorCode;
import com.algangi.mongle.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class MemberFinder {

    private final MemberRepository memberRepository;

    public Member getMemberOrThrow(String memberId) {
        validateMemberId(memberId);
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Member getMemberWithLockOrThrow(String memberId) {
        validateMemberId(memberId);
        return memberRepository.findByIdWithLock(memberId)
            .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public List<Member> findMembersByIds(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }
        return memberRepository.findAllByMemberIdIn(memberIds);
    }

    private void validateMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }
}