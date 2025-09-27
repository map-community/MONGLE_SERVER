package com.algangi.mongle.dynamicCloud.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.global.domain.service.CellService;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DynamicCloudFormationService {

    private final DynamicCloudRepository dynamicCloudRepository;
    private final PostRepository postRepository;
    private final CellService cellService;

    public DynamicCloud createDynamicCloudAndMergeIfNeeded(String s2TokenId,
        List<Post> existingPostsInCell) {
        // 1. 새로운 동적 구름 생성
        DynamicCloud newDynamicCloud = DynamicCloud.create(Set.of(s2TokenId));

        // 2. 인접 동적 구름 조회
        Set<String> adjacentS2TokenIds = cellService.getAdjacentCells(s2TokenId);
        List<DynamicCloud> adjacentClouds = dynamicCloudRepository.findActiveCloudsInCells(
            adjacentS2TokenIds);

        DynamicCloud finalCloud = newDynamicCloud;
        if (!adjacentClouds.isEmpty()) {
            // 3. 인접 구름이 있으면 병합 로직 실행
            finalCloud = mergeWithAdjacentClouds(newDynamicCloud, adjacentClouds);
        }

        // 4. 최종 구름 저장 (새 구름이거나, 병합의 중심이 된 가장 오래된 구름)
        DynamicCloud savedCloud = dynamicCloudRepository.save(finalCloud);

        // 5. 기존 알갱이 상태 게시물들을 동적 구름에 할당
        if (!existingPostsInCell.isEmpty()) {
            existingPostsInCell.forEach(post -> post.assignToDynamicCloud(savedCloud.getId()));
            postRepository.saveAll(existingPostsInCell);
        }

        return savedCloud;
    }

    private DynamicCloud mergeWithAdjacentClouds(DynamicCloud newCloud,
        List<DynamicCloud> adjacentClouds) {
        List<DynamicCloud> allRelatedClouds = new ArrayList<>(adjacentClouds);
        allRelatedClouds.add(newCloud);

        DynamicCloud oldestCloud = allRelatedClouds.stream()
            .min(Comparator.comparing(DynamicCloud::getCreatedDate,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(newCloud);

        List<DynamicCloud> cloudsToBeMerged = allRelatedClouds.stream()
            .filter(cloud -> !cloud.getId().equals(oldestCloud.getId()))
            .toList();

        if (cloudsToBeMerged.isEmpty()) {
            return oldestCloud;
        }

        List<Long> cloudIdsToBeMerged = cloudsToBeMerged.stream().map(DynamicCloud::getId).toList();
        List<Post> postsToReassign = postRepository.findByDynamicCloudIdIn(cloudIdsToBeMerged);

        postsToReassign.forEach(post -> post.assignToDynamicCloud(oldestCloud.getId()));
        cloudsToBeMerged.forEach(oldestCloud::mergeWith);

        postRepository.saveAll(postsToReassign);
        // dynamicCloudRepository.deleteAll(cloudsToBeMerged); // 비활성화 처리 또는 삭제 정책 결정 필요

        return oldestCloud;
    }
}
