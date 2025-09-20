package com.algangi.mongle.post.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.global.infrastructure.S2CellService;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostCreationCommand;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCreationService {

    private final StaticCloudRepository staticCloudRepository;
    private final DynamicCloudRepository dynamicCloudRepository;
    private final PostRepository postRepository;
    private final S2CellService s2CellService;

    @Transactional
    public Post createPost(PostCreationCommand command) {
        String s2TokenId = command.s2TokenId();

        // 1. 정적 구름 존재 여부 확인
        Optional<StaticCloud> staticCloud = staticCloudRepository.findByS2TokenId(s2TokenId);
        if (staticCloud.isPresent()) {
            return createPostInStaticCloud(command, staticCloud.get());
        }

        // 2. 동적 구름 존재 여부 확인
        Optional<DynamicCloud> existingDynamicCloud = dynamicCloudRepository.findActiveByS2TokenId(s2TokenId);
        if (existingDynamicCloud.isPresent()) {
            return createPostInExistingDynamicCloud(command, existingDynamicCloud.get());
        }

        // 3. 동적 구름이 없는 경우 - 게시물 개수에 따른 처리
        return handleNewCellPost(command, s2TokenId);
    }

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

    private Post createPostInExistingDynamicCloud(PostCreationCommand command, DynamicCloud dynamicCloud) {
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

    private Post handleNewCellPost(PostCreationCommand command, String s2TokenId) {
        // 해당 셀의 기존 게시물 개수 확인
        List<Post> existingPostsInCell = postRepository.findByS2TokenIdWithLock(s2TokenId);
        int totalPostCount = existingPostsInCell.size() + 1; // 현재 생성할 게시물 포함

        if (totalPostCount <= 2) {
            // 알갱이 상태로 게시물 생성
            return createStandalonePost(command, s2TokenId);
        } else {
            // 동적 구름 생성 필요 - 인접 동적 구름 유무에 따른 처리
            return createPostWithNewDynamicCloud(command, s2TokenId, existingPostsInCell);
        }
    }

    private Post createStandalonePost(PostCreationCommand command, String s2TokenId) {
        Post post = Post.createStandalone(
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId()
        );
        return postRepository.save(post);
    }

    private Post createPostWithNewDynamicCloud(PostCreationCommand command, String s2TokenId,
        List<Post> existingPosts) {

        // 새로운 동적 구름 생성
        DynamicCloud newDynamicCloud = DynamicCloud.create(Set.of(s2TokenId));

        // 인접 동적 구름 조회
        List<DynamicCloud> adjacentClouds = findAdjacentDynamicClouds(s2TokenId);

        // 인접한 동적 구름이 있을 경우 생성일 가장 오래된 동적 구름 기준으로 병합 처리
        // 인접한 동적 구름 없을 경우 새로 생성한 동적 구름 사용
        if (!adjacentClouds.isEmpty()) {
            newDynamicCloud = mergeWithAdjacentClouds(newDynamicCloud, adjacentClouds);
        }

        // 가장 오래된 동적 구름 update or 새로운 동적 구름 저장
        DynamicCloud savedCloud = dynamicCloudRepository.save(newDynamicCloud);

        // 기존 알갱이 게시물들을 동적 구름에 할당 (새로 생성되는 동적 구름의 경우 IDENTITY 전략에 의해 save 후 id가 생성됨)
        existingPosts.forEach(post -> post.assignToDynamicCloud(savedCloud.getId()));
        postRepository.saveAll(existingPosts);

        // 새 게시물 생성
        Post newPost = Post.createInDynamicCloud(
            command.location(),
            command.s2TokenId(),
            command.title(),
            command.content(),
            command.authorId(),
            savedCloud.getId()
        );

        return postRepository.save(newPost);
    }

    private List<DynamicCloud> findAdjacentDynamicClouds(String s2TokenId) {
        Set<String> adjacentS2TokenIds = s2CellService.getAdjacentCells(s2TokenId);
        return dynamicCloudRepository.findActiveCloudsInCells(adjacentS2TokenIds);
    }

    private DynamicCloud mergeWithAdjacentClouds(DynamicCloud newCloud, List<DynamicCloud> adjacentClouds) {
        // 1. 병합에 관련된 모든 구름을 하나의 리스트로 통합
        List<DynamicCloud> allRelatedClouds = new ArrayList<>(adjacentClouds);
        allRelatedClouds.add(newCloud);

        // 2. 가장 오래된 구름을 병합 중심으로 결정
        DynamicCloud oldestCloud = allRelatedClouds.stream()
            .min(Comparator.comparing(DynamicCloud::getCreatedDate,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(newCloud);

        // 3. 병합될 구름들(가장 오래된 구름 제외)을 필터링하여 리스트로 수집
        List<DynamicCloud> cloudsToBeMerged = allRelatedClouds.stream()
            .filter(cloud -> !cloud.getId().equals(oldestCloud.getId()))
            .toList();

        if (!cloudsToBeMerged.isEmpty()) {
            // 4. 병합될 구름들의 ID 목록 추출
            List<Long> cloudIdsToBeMerged = cloudsToBeMerged.stream()
                .map(DynamicCloud::getId)
                .toList();

            // 5. 병합될 모든 게시물들을 단 한 번의 쿼리로 조회 (DB 조회 최적화)
            List<Post> postsToReassign = postRepository.findByDynamicCloudIdIn(cloudIdsToBeMerged);

            // 6. 게시물들의 소속을 oldestCloud로 변경하고, oldestCloud에 셀 정보 병합
            postsToReassign.forEach(post -> post.assignToDynamicCloud(oldestCloud.getId()));
            cloudsToBeMerged.forEach(oldestCloud::mergeWith); // forEach로 각 구름의 정보를 병합

            // 7. 변경된 게시물들과 병합된 구름 정보를 한번에 저장 (DB 업데이트 최적화)
            postRepository.saveAll(postsToReassign);
            dynamicCloudRepository.save(oldestCloud);

            // 8. 병합되어 비어버린 구름들을 DB에서 삭제 (보류)
            // dynamicCloudRepository.deleteAll(cloudsToBeMerged);
        }

        return oldestCloud;
    }

}
