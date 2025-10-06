package com.algangi.mongle.post.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.post.application.service.PostEventPublisher;
import com.algangi.mongle.post.event.PostFileUpdatedEvent;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsPostEventPublisher implements PostEventPublisher {

    private final SqsTemplate sqsTemplate;

    @Value("${mongle.aws.sqs.post-file-update-queue-name}")
    private String queueName;

    @Override
    public void publish(PostFileUpdatedEvent event) {
        try {
            sqsTemplate.send(to -> to.queue(queueName).payload(event));
        } catch (Exception e) {
            log.error("Failed to publish PostFileUpdatedEvent to SQS. PostId: {}, Error: {}",
                event.postId(), e.getMessage(), e);
            throw new ApplicationException(AwsErrorCode.SQS_PUBLISH_FAILED, e)
                .addErrorInfo("postId", event.postId())
                .addErrorInfo("queueName", queueName);
        }
    }
}