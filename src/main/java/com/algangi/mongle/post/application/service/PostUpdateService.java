package com.algangi.mongle.post.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.service.PostFileCommitValidationService;
import com.algangi.mongle.post.event.PostFileUpdatedEvent;
import com.algangi.mongle.post.presentation.dto.PostUpdateRequest;
import com.algangi.mongle.post.presentation.dto.PostUpdateResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostUpdateService {

    private final PostFinder postFinder;
    private final PostEventPublisher postEventPublisher;
    private final PostFileCommitValidationService postFileCommitValidationService;

    @Transactional
    public PostUpdateResponse updatePost(String postId, PostUpdateRequest request,
        String memberId) {
        Post post = postFinder.getPostWithLockOrThrow(postId);
        if (!post.getAuthorId().equals(memberId)) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }
        post.markAsUploading();

        List<String> finalFileKeys = request.fileKeyList();
        List<PostFile> finalPostFiles = finalFileKeys.stream()
            .map(PostFile::create)
            .toList();

        List<String> currentFileKeys = post.getPostFiles().stream()
            .map(PostFile::getFileKey)
            .toList();
        List<String> keysToAdd = finalFileKeys.stream()
            .filter(key -> !currentFileKeys.contains(key)).toList();

        postFileCommitValidationService.validateTemporaryFiles(keysToAdd);

        post.updatePost(request.content(), finalPostFiles);

        PostFileUpdatedEvent event = new PostFileUpdatedEvent(postId, finalFileKeys);
        postEventPublisher.publish(event);

        return PostUpdateResponse.of(postId, request.content(), finalFileKeys);
    }
}
