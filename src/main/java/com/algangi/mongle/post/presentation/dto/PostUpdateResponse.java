package com.algangi.mongle.post.presentation.dto;

import java.util.List;

public record PostUpdateResponse(
    String id,
    String content,
    List<String> fileKeys
) {

    public static PostUpdateResponse of(String id, String content, List<String> fileKeys) {
        return new PostUpdateResponse(id, content, fileKeys);
    }
}
