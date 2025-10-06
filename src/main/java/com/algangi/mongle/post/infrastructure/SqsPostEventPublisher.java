package com.algangi.mongle.post.infrastructure;

package com.algangi.mongle.post.infrastructure.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.algangi.mongle.post.application.service.PostEventPublisher;
import com.algangi.mongle.post.event.PostFileUpdatedEvent;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqsPostEventPublisher implements PostEventPublisher {

    private final SqsTemplate sqsTemplate;

    @Value("${mongle.aws.sqs.post-file-update-queue-name}")
    private String queueName;

    @Override
    public void publish(PostFileUpdatedEvent event) {
        sqsTemplate.send(to -> to.queue(queueName).payload(event));
    }
}