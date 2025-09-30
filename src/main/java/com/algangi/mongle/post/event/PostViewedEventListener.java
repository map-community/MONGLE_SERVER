package com.algangi.mongle.post.event;

import com.algangi.mongle.stats.application.service.ContentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostViewedEventListener {

    private final ContentStatsService contentStatsService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostView(PostViewedEvent event) {
        contentStatsService.incrementPostViewCount(event.postId());
    }

}