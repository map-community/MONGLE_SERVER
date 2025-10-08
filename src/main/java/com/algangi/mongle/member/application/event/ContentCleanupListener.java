package com.algangi.mongle.member.application.event;

import com.algangi.mongle.member.application.service.WithdrawalCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ContentCleanupListener {

    private final WithdrawalCleanupService withdrawalCleanupService;

    @Async("statsUpdateTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentAndReactions(MemberWithdrawnEvent event) {
        withdrawalCleanupService.cleanupDataFor(event);
    }
}
