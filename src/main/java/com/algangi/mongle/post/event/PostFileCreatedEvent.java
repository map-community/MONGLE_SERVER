package com.algangi.mongle.post.event;

import java.util.List;

public record PostFileCreatedEvent(
    String postId,
    List<String> temporaryFileKeys
) {

}
