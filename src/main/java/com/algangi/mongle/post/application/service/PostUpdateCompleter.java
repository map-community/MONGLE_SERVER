package com.algangi.mongle.post.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostUpdateCompleter {

    private final PostFinder postFinder;

    @Transactional
    public void completePostUpdate(String postId, List<PostFile> finalPostFiles) {
        Post post = postFinder.getPostOrThrow(postId);
        post.markAsActive();
        post.updatePostFiles(finalPostFiles);
    }

}
