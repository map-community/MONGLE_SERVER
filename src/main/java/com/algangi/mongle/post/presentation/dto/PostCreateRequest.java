package com.algangi.mongle.post.presentation.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
    @NotNull(message = "위도는 필수값입니다.")
    Double latitude,
    @NotNull(message = "경도는 필수값입니다.")
    Double longitude,
    @NotBlank(message = "게시글 제목은 필수값입니다.")
    @Size(max = 100)
    String title,
    @NotBlank(message = "게시글 내용은 필수값입니다.")
    @Size(max = 2000)
    String content,
    @NotNull(message = "작성자 ID는 필수값입니다.")
    Long authorId,//인증 도입 시 제거 예정
    List<String> fileKeyList
) {

}
