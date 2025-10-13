package com.algangi.mongle.post.application.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.event.PostFileUpdatedEvent;
import com.algangi.mongle.post.presentation.dto.PostUpdateRequest;
import com.algangi.mongle.post.presentation.dto.PostUpdateResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostUpdateService {

    private final PostFinder postFinder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PostUpdateResponse updatePost(String postId, PostUpdateRequest request,
        String memberId) {
        Post post = postFinder.getPostWithLockOrThrow(postId);
        if (!post.getAuthorId().equals(memberId)) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }
        post.markAsPending();

        List<String> previousFileKeys = post.getPostFiles().stream()
            .map(PostFile::getFileKey)
            .toList();
        List<String> finalFileKeys = request.fileKeyList();

        post.updateContent(request.content());

        PostFileUpdatedEvent event = new PostFileUpdatedEvent(postId, previousFileKeys,
            finalFileKeys);
        eventPublisher.publishEvent(event);

        return PostUpdateResponse.of(postId, request.content());
    }
}
