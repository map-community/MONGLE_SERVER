package com.algangi.mongle.comment.application.event;

import com.algangi.mongle.stats.application.service.ContentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostStatsListener {

    private final ContentStatsService contentStatsService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreation(CommentCreatedEvent event) {
        contentStatsService.incrementPostCommentCount(event.postId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeletion(CommentDeletedEvent event) {
        contentStatsService.decrementPostCommentCount(event.postId());
    }
}
