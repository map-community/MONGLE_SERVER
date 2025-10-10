package com.algangi.mongle.auth.event;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.file.domain.FileType;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberSignedUpEventListener {

    private final FileService fileService;
    private final MemberFinder memberFinder;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberSignedUpEvent(MemberSignedUpEvent event) {
        if (event.temporaryProfileImageKey() == null) {
            return;
        }

        List<String> fileKeyList = List.of(event.temporaryProfileImageKey());

        List<String> permanentKeys = fileService.commitFiles(
            FileType.PROFILE_IMAGE,
            event.memberId(),
            fileKeyList
        );

        Member member = memberFinder.getMemberOrThrow(event.memberId());
        member.updateProfileImage(permanentKeys.getFirst());
    }
}

