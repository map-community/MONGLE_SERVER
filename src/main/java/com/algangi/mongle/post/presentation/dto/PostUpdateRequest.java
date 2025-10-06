package com.algangi.mongle.post.presentation.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
    @NotBlank(message = "게시글 내용은 필수값입니다.")
    @Size(max = 2000)
    String content,
    List<String> fileKeyList
) {

    public PostUpdateRequest {
        fileKeyList = Optional.ofNullable(fileKeyList).orElse(Collections.emptyList());
    }
}
