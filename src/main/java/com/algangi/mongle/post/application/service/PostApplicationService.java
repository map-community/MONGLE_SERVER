package com.algangi.mongle.post.application.service;

import org.springframework.stereotype.Service;

import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostCreationCommand;
import com.algangi.mongle.post.domain.service.PostCreationService;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;
import com.algangi.mongle.post.presentation.dto.PostResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final PostCreationService postCreationService;

    public PostResponse createPost(PostCreateRequest request) {
        PostCreationCommand command = new PostCreationCommand(
            Location.create(request.latitude(), request.longitude()),
            request.s2TokenId(),
            request.title(),
            request.content(),
            request.authorId()
        );

        Post createdPost = postCreationService.createPost(command);

        return PostResponse.from(createdPost);
    }

}
