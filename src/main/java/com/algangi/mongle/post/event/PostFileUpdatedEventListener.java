package com.algangi.mongle.post.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.file.domain.FileType;
import com.algangi.mongle.post.application.service.PostUpdateCompleter;
import com.algangi.mongle.post.domain.model.PostFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostFileUpdatedEventListener {

    private final FileService fileService;
    private final PostUpdateCompleter postUpdateCompleter;

    @Async("fileTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
            List<String> movedPermanentKeys = fileService.commitFiles(FileType.POST_FILE,
                event.postId(),
                keysToAdd);
            //파일 삭제 (삭제된 파일 영구 저장소에서 삭제)
            fileService.deletePermanentFiles(keysToDelete);

            List<String> retainedFileKeys = event.previousFileKeys().stream()
                .filter(event.finalFileKeys()::contains).toList();

            List<PostFile> finalPostFiles = new ArrayList<>();
            retainedFileKeys.stream().map(PostFile::create).forEach(finalPostFiles::add);
            movedPermanentKeys.stream().map(PostFile::create).forEach(finalPostFiles::add);

            postUpdateCompleter.completePostUpdate(event.postId(), finalPostFiles);
        } catch (Exception e) {
            log.error("게시물 업데이트 후 파일 후속처리(이동 및 삭제) 작업 실패 : {}. Error: {}", event.postId(),
                e.getMessage(), e);
        }
    }
}
