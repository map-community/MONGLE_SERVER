package com.algangi.mongle.post.application.helper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.exception.PostErrorCode;
import com.algangi.mongle.post.application.dto.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class PostFinder {

    private final PostRepository postRepository;

    public Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new ApplicationException(PostErrorCode.POST_NOT_FOUND));
    }

}
