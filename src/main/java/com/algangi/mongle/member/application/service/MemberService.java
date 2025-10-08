package com.algangi.mongle.member.application.service;

import com.algangi.mongle.member.application.event.MemberWithdrawnEvent;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ApplicationEventPublisher eventPublisher;
    private final MemberFinder memberFinder;

    @Transactional
    public void withdrawMember(String memberId) {
        Member member = memberFinder.getMemberOrThrow(memberId);
        member.deactivate();
        eventPublisher.publishEvent(new MemberWithdrawnEvent(memberId));
    }
}