package com.algangi.mongle.post.domain.repository;

import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;


public record PostCreationCommand (
    Location location,
    String s2TokenId,
    String title,
    String content,
    Long authorId
){
    public static PostCreationCommand from(PostCreateRequest dto){
        return new PostCreationCommand(
            Location.create(dto.latitude(), dto.longitude()),
            dto.s2TokenId(),
            dto.title(),
            dto.content(),
            dto.authorId()
        );
    }

}
