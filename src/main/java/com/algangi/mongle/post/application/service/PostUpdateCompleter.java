package com.algangi.mongle.post.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostUpdateCompleter {

    private final PostFinder postFinder;
    private final PostRepository postRepository;

    @Transactional
    public void completePostUpdate(String postId, List<PostFile> finalPostFiles) {
        Post post = postFinder.getPostOrThrow(postId);
        post.markAsActive();
        post.updatePostFiles(finalPostFiles);
        postRepository.save(post);
    }

}
