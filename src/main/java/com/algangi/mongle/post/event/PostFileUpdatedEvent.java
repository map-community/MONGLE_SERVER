package com.algangi.mongle.post.event;

import java.util.List;

public record PostFileUpdatedEvent(
    String postId,
    List<String> finalFileKeys
) {

}
