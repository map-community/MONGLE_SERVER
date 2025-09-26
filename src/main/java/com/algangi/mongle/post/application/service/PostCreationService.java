package com.algangi.mongle.post.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.dynamicCloud.domain.service.DynamicCloudFormationService;
import com.algangi.mongle.post.application.dto.PostCreationCommand;
import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.post.domain.service.PostFileCommitValidationService;
import com.algangi.mongle.post.domain.service.PostIdService;
import com.algangi.mongle.post.event.PostFileCommitEvent;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;
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
    private final DynamicCloudFormationService dynamicCloudFormationService;
    private final PostFileCommitValidationService postFileCommitValidationService;
    private final PostIdService postIdService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        String postId = postIdService.createId();
        String s2TokenId = request.s2TokenId(); //TODO: s2Token 라이브러리로 계산

        PostCreationCommand command = PostCreationCommand.of(
            postId,
            Location.create(request.latitude(), request.longitude()),
            s2TokenId,
            request.title(),
            request.content(),
            request.authorId());

        Post createdPost;
        // 1. 정적 구름 존재 여부 확인
        Optional<StaticCloud> staticCloud = staticCloudRepository.findByS2TokenId(s2TokenId);
        if (staticCloud.isPresent()) {
            createdPost = createPostInStaticCloud(command, staticCloud.get());
        }

        // 2. 동적 구름 존재 여부 확인
        Optional<DynamicCloud> existingDynamicCloud = dynamicCloudRepository.findActiveByS2TokenId(
            s2TokenId);
        if (existingDynamicCloud.isPresent()) {
            createdPost = createPostInDynamicCloud(command, existingDynamicCloud.get());
        }

        // 3. 동적 구름이 없는 경우
        createdPost = handleNewPost(command, s2TokenId);

        // 4. 임시 PostFile 검증
        postFileCommitValidationService.validateTemporaryFiles(request.fileKeys());
        Post savedPost = postRepository.save(createdPost);

        eventPublisher.publishEvent(new PostFileCommitEvent(savedPost.getId(), request.fileKeys()));
        return PostResponse.from(savedPost);
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
            DynamicCloud targetCloud = dynamicCloudFormationService.createDynamicCloudAndMergeIfNeeded(
                s2TokenId, existingPostsInCell);
            return createPostInDynamicCloud(command, targetCloud);
        }
    }


    //게시물 생성 헬퍼 메서드
    private Post createPostInStaticCloud(PostCreationCommand command, StaticCloud staticCloud) {
        return Post.createInStaticCloud(
            command.id(),
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId(),
            staticCloud.getId()
        );
    }

    private Post createPostInDynamicCloud(PostCreationCommand command, DynamicCloud dynamicCloud) {
        return Post.createInDynamicCloud(
            command.id(),
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId(),
            dynamicCloud.getId()
        );
    }

    private Post createStandalonePost(PostCreationCommand command) {
        return Post.createStandalone(
            command.id(),
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId()
        );
    }

}
