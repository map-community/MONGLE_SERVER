package com.algangi.mongle.map.application.service;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.global.infrastructure.S2CellService;
import com.algangi.mongle.map.presentation.dto.MapObjectsRequest;
import com.algangi.mongle.map.presentation.dto.MapObjectsResponse;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapQueryService {

    private final S2CellService s2CellService;
    private final PostQueryRepository postQueryRepository;
    private final StaticCloudRepository staticCloudRepository;
    private final DynamicCloudRepository dynamicCloudRepository;
    private final MemberFinder memberFinder;

    public MapObjectsResponse getMapObjects(MapObjectsRequest request) {
        List<String> s2cellTokens = s2CellService.getCellsForRect(
            request.swLat(), request.swLng(), request.neLat(), request.neLng()
        );

        if (s2cellTokens.isEmpty()) {
            return MapObjectsResponse.empty();
        }

        List<Post> grains = postQueryRepository.findGrainsInCells(s2cellTokens);
        List<StaticCloud> staticClouds = staticCloudRepository.findCloudsInCells(s2cellTokens);
        List<DynamicCloud> dynamicClouds = dynamicCloudRepository.findActiveCloudsInCells(
            s2cellTokens);

        Map<Long, Member> authors = getAuthorsForGrains(grains);

        List<MapObjectsResponse.Grain> grainDtos = grains.stream()
            .map(post -> {
                Member author = authors.get(post.getAuthorId());
                String profileImageUrl = (author != null) ? author.getProfileImage() : null;
                return new MapObjectsResponse.Grain(
                    post.getId(),
                    post.getLocation().getLatitude(),
                    post.getLocation().getLongitude(),
                    profileImageUrl
                );
            })
            .toList();

        List<MapObjectsResponse.StaticCloudInfo> staticCloudDtos = staticClouds.stream()
            .map(cloud -> new MapObjectsResponse.StaticCloudInfo(
                cloud.getId().toString(),
                cloud.getName(),
                cloud.getLatitude(),
                cloud.getLongitude(),
                // TODO: 해당 구름의 게시글 수 집계 로직 필요
                42,
                // TODO: 구름의 폴리곤 좌표 생성 로직 필요
                Collections.emptyList()
            ))
            .toList();

        List<MapObjectsResponse.DynamicCloudInfo> dynamicCloudDtos = dynamicClouds.stream()
            .map(cloud -> new MapObjectsResponse.DynamicCloudInfo(
                cloud.getId().toString(),
                // TODO: 해당 구름의 게시글 수 집계 로직 필요
                15,
                // TODO: 구름의 폴리곤 좌표 생성 로직 필요
                Collections.emptyList()
            ))
            .toList();

        return new MapObjectsResponse(grainDtos, staticCloudDtos, dynamicCloudDtos);
    }

    private Map<Long, Member> getAuthorsForGrains(List<Post> grains) {
        if (grains.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> authorIds = grains.stream()
            .map(Post::getAuthorId)
            .distinct()
            .toList();
        return memberFinder.findMembersByIds(authorIds).stream()
            .collect(Collectors.toMap(Member::getMemberId, Function.identity()));
    }
}

