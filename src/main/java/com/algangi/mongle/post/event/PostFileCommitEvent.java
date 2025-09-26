package com.algangi.mongle.post.event;

import java.util.Set;

public record PostFileCommitEvent(
    String postId,
    Set<String> temporaryFileKeys
) {

}
