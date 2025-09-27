package com.algangi.mongle.post.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.service.PostFileMover;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostFileCreatedEventListener {

    private final PostFileMover postFileMover;
    private final PostFinder postFinder;

    @Async("fileTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFileCommit(PostFileCreatedEvent event) {
        log.info("비동기 파일 이동 시작: PostId={}", event.postId());
        try {
            Post post = postFinder.getPostOrThrow(event.postId());
            List<String> permanentKeys = postFileMover.moveBulkTempToPermanent(event.postId(),
                event.temporaryFileKeys());

            List<PostFile> postFiles = permanentKeys.stream()
                .map(PostFile::create)
                .toList();

            post.addPostFiles(postFiles);
            post.markAsActive();

        } catch (Exception e) {
            // 이 트랜잭션은 롤백되지만, 이미 S3에서 일부 파일이 이동되었을 수 있음 (복구 로직 필요)
            log.error("비동기 파일 처리 중 심각한 오류 발생. PostId: {}", event.postId(), e);
        }
    }
}
