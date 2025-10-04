package com.algangi.mongle.post.application.service;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.exception.PostErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandService {

    private final PostFinder postFinder;

    public void deletePost(String postId, String memberId) {
        Post post = postFinder.getPostOrThrow(postId);
        
        if (!Objects.equals(post.getAuthorId(), memberId)) {
            throw new ApplicationException(PostErrorCode.POST_ACCESS_DENIED);
        }

        post.softDeleteByUser();
    }
}