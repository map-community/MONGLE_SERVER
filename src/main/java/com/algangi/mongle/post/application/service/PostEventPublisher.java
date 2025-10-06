package com.algangi.mongle.post.application.service;

import com.algangi.mongle.post.event.PostFileUpdatedEvent;

public interface PostEventPublisher {

    void publish(PostFileUpdatedEvent event);
}
