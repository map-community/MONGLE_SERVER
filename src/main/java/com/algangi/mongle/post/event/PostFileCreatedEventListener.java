package com.algangi.mongle.post.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.file.domain.FileType;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostFileCreatedEventListener {

    private final FileService fileService;
    private final PostFinder postFinder;

    @Async("fileTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFileCommit(PostFileCreatedEvent event) {
        log.info("게시물 파일 커밋/활성화 작업 시작: PostId={}", event.postId());
        try {
            Post post = postFinder.getPostOrThrow(event.postId());

            // 파일이 있는 경우에만 파일 이동 로직 수행
            if (!event.temporaryFileKeys().isEmpty()) {
                List<String> permanentKeys = fileService.commitFiles(FileType.POST_FILE,
                    event.postId(),
                    event.temporaryFileKeys());

                List<PostFile> postFiles = permanentKeys.stream()
                    .map(PostFile::create)
                    .toList();
                post.addPostFiles(postFiles);
            }

            // 파일 유무와 상관없이 항상 ACTIVE 상태로 변경
            post.markAsActive();

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.error("게시물 파일 커밋작업 중 예상치 못한 오류 발생. PostId={}", event.postId(), e);
            throw new ApplicationException(AwsErrorCode.S3_UNKNOWN_ERROR, e)
                .addErrorInfo("postId", event.postId());
        }
    }
}
