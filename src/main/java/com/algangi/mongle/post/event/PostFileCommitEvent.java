package com.algangi.mongle.post.event;

import java.util.List;

public record PostFileCommitEvent(
    String postId,
    List<String> temporaryFileKeys
) {

}
