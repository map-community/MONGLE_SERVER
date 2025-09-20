package com.algangi.mongle.post.domain.repository;

import com.algangi.mongle.post.domain.model.Location;


public record PostCreationCommand (
    Location location,
    String s2TokenId,
    String title,
    String content,
    Long authorId
){

}
