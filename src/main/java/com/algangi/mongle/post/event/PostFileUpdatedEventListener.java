package com.algangi.mongle.post.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.algangi.mongle.post.application.service.PostUpdateCompleter;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.service.PostFileHandler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostFileUpdatedEventListener {

    private final PostFileHandler postFileHandler;
    private final PostUpdateCompleter postUpdateCompleter;

    @SqsListener(value = "${mongle.aws.sqs.post-file-update-queue-name}",
        acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
    public void handleFileUpdateEvent(PostFileUpdatedEvent event) {
        log.info("Receiving SQS message for post: {}", event.postId());
        try {
            List<String> keysToAdd = event.finalFileKeys().stream()
                .filter(key -> !event.previousFileKeys().contains(key))
                .toList();
            List<String> keysToDelete = event.previousFileKeys().stream()
                .filter(key -> !event.finalFileKeys().contains(key))
                .toList();
            log.info("Adding files to post: {}", keysToAdd);
            log.info("Deleting files from post: {}", keysToDelete);

            List<String> movedPermanentKeys = postFileHandler.moveBulkTempToPermanent(
                event.postId(),
                keysToAdd);
            postFileHandler.deletePermanentFiles(keysToDelete);

            List<String> retainedFileKeys = event.previousFileKeys().stream()
                .filter(event.finalFileKeys()::contains).toList();

            List<PostFile> finalPostFiles = new ArrayList<>();
            retainedFileKeys.stream().map(PostFile::create).forEach(finalPostFiles::add);
            movedPermanentKeys.stream().map(PostFile::create).forEach(finalPostFiles::add);

            postUpdateCompleter.completePostUpdate(event.postId(), finalPostFiles);

        } catch (Exception e) {
            log.error("게시물 업데이트 후 파일 후속처리(이동 및 삭제) 작업 실패 : {}. Error: {}", event.postId(),
                e.getMessage());
            throw e;
        }
    }
}
