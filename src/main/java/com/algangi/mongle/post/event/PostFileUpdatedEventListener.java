package com.algangi.mongle.post.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostFileUpdatedEventListener {

    private final FileService fileService;
    private final PostFinder postFinder;

    @Async("fileTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFileUpdatedEvent(PostFileUpdatedEvent event) {
        log.info("Receiving SQS message for post: {}", event.postId());
        try {
            List<String> keysToAdd = event.finalFileKeys().stream()
                .filter(key -> !event.previousFileKeys().contains(key))
                .toList();
            List<String> keysToDelete = event.previousFileKeys().stream()
                .filter(key -> !event.finalFileKeys().contains(key))
                .toList();

            //파일 커밋 (추가된 파일 임시 저장소에서 영구 저장소로 이동)
            if (!keysToAdd.isEmpty()) {
                fileService.commitFiles(keysToAdd);
            }
            //파일 삭제 (삭제된 파일 영구 저장소에서 삭제)
            if (!keysToDelete.isEmpty()) {
                fileService.deletePermanentFiles(keysToDelete);
            }

            Post post = postFinder.getPostOrThrow(event.postId());
            post.markAsActive();

            List<PostFile> postFiles = event.finalFileKeys().stream()
                .map(PostFile::create)
                .toList();
            post.updatePostFiles(postFiles);
        } catch (Exception e) {
            log.error("게시물 업데이트 후 파일 후속처리(이동 및 삭제) 작업 실패 : {}. Error: {}", event.postId(),
                e.getMessage(), e);
        }
    }
}
