package com.algangi.mongle.post.event;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.repository.PostRepository;
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
    private final PostRepository postRepository;

    @SqsListener(value = "${mongle.aws.sqs.post-file-update-queue-name}",
        acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
    @Transactional
    public void handleFileUpdateEvent(PostFileUpdatedEvent event) {
        log.info("Receiving SQS message for post: {}", event.postId());
        try {
            Post post = postRepository.findById(event.postId()).orElseThrow();
            List<String> currentFileKeys = post.getPostFiles().stream()
                .map(PostFile::getFileKey)
                .toList();

            List<String> keysToAdd = event.finalFileKeys().stream()
                .filter(key -> !currentFileKeys.contains(key)).toList();
            List<String> keysToDelete = currentFileKeys.stream()
                .filter(key -> !event.finalFileKeys().contains(key)).toList();

            postFileHandler.moveBulkTempToPermanent(post.getId(), keysToAdd);
            postFileHandler.deletePermanentFiles(keysToDelete);

            post.markAsActive();
            postRepository.save(post);

        } catch (Exception e) {
            log.error("게시물 업데이트 후 파일 후속처리(이동 및 삭제) 작업 실패 : {}. Error: {}", event.postId(),
                e.getMessage());
            throw new ApplicationException(AwsErrorCode.S3_UNKNOWN_ERROR, e)
                .addErrorInfo("postId", event.postId());
        }
    }
}
