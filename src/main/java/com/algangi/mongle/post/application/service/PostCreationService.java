package com.algangi.mongle.post.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.dynamicCloud.domain.service.DynamicCloudService;
import com.algangi.mongle.post.application.dto.PostRepository;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostCreationCommand;
import com.algangi.mongle.post.presentation.dto.PostResponse;
import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCreationService {

    private static final int DYNAMIC_CLOUD_CREATION_THRESHOLD = 2;
    private final StaticCloudRepository staticCloudRepository;
    private final DynamicCloudRepository dynamicCloudRepository;
    private final PostRepository postRepository;
    private final DynamicCloudService dynamicCloudService;

    @Transactional
    public PostResponse createPost(PostCreationCommand command) {
        String s2TokenId = command.s2TokenId();

        // 1. 정적 구름 존재 여부 확인
        Optional<StaticCloud> staticCloud = staticCloudRepository.findByS2TokenId(s2TokenId);
        if (staticCloud.isPresent()) {
            return PostResponse.from(createPostInStaticCloud(command, staticCloud.get()));
        }

        // 2. 동적 구름 존재 여부 확인
        Optional<DynamicCloud> existingDynamicCloud = dynamicCloudRepository.findActiveByS2TokenId(
            s2TokenId);
        if (existingDynamicCloud.isPresent()) {
            return PostResponse.from(createPostInDynamicCloud(command, existingDynamicCloud.get()));
        }

        // 3. 동적 구름이 없는 경우
        return PostResponse.from(handleNewPost(command, s2TokenId));
    }

    private Post handleNewPost(PostCreationCommand command, String s2TokenId) {
        List<Post> existingPostsInCell = postRepository.findByS2TokenIdWithLock(s2TokenId);
        // Concurrency Gap 방지 로직
        Optional<DynamicCloud> cloudAfterLock = dynamicCloudRepository.findActiveByS2TokenId(
            s2TokenId);
        if (cloudAfterLock.isPresent()) {
            return createPostInDynamicCloud(command, cloudAfterLock.get());
        }

        // 게시물 개수에 따른 처리
        int totalPostCount = existingPostsInCell.size() + 1;
        if (totalPostCount <= DYNAMIC_CLOUD_CREATION_THRESHOLD) {
            return createStandalonePost(command);
        } else {
            // DynamicCloudService에 동적 구름 생성 및 병합 책임을 위임
            DynamicCloud targetCloud = dynamicCloudService.createDynamicCloudAndMergeIfNeeded(
                s2TokenId, existingPostsInCell);
            return createPostInDynamicCloud(command, targetCloud);
        }
    }


    //게시물 생성 헬퍼 메서드
    private Post createPostInStaticCloud(PostCreationCommand command, StaticCloud staticCloud) {
        Post post = Post.createInStaticCloud(
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId(),
            staticCloud.getId()
        );
        return postRepository.save(post);
    }

    private Post createPostInDynamicCloud(PostCreationCommand command, DynamicCloud dynamicCloud) {
        Post post = Post.createInDynamicCloud(
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId(),
            dynamicCloud.getId()
        );
        return postRepository.save(post);
    }

    private Post createStandalonePost(PostCreationCommand command) {
        Post post = Post.createStandalone(
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId()
        );
        return postRepository.save(post);
    }

}
