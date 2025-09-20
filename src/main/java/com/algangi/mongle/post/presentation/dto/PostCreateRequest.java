package com.algangi.mongle.post.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
    @NotNull(message = "위도는 필수값입니다.")
    Double latitude,
    @NotNull(message = "경도는 필수값입니다.")
    Double longitude,
    @NotBlank(message = "S2 Cell 토큰 ID는 필수값입니다.")
    String s2TokenId,
    @NotBlank(message = "게시글 제목은 필수값입니다.")
    String title,
    @NotBlank(message = "게시글 내용은 필수값입니다.")
    String content,
    @NotNull(message = "작성자 ID는 필수값입니다.")
    Long authorId
) {

}
