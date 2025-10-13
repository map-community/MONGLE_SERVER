package com.algangi.mongle.auth.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.algangi.mongle.file.application.service.FileService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberSignedUpEventListener {

    private final FileService fileService;

    @Async("fileTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberSignedUpEvent(MemberSignedUpEvent event) {
        if (event.profileImageKey() != null) {
            List<String> fileKeyList = List.of(event.profileImageKey());
            fileService.commitFiles(fileKeyList);
        }
    }
}

