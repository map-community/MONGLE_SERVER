package com.algangi.mongle.map.application.service;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.global.infrastructure.S2CellService;
import com.algangi.mongle.global.util.S2PolygonConverter;
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
    private final S2PolygonConverter s2PolygonConverter;

    public MapObjectsResponse getMapObjects(MapObjectsRequest request) {
        // 1. 요청된 위경도 사각 영역을 커버하는 S2 Cell 목록을 가져옵니다.
        List<String> s2cellTokens = s2CellService.getCellsForRect(
            request.swLat(), request.swLng(), request.neLat(), request.neLng()
        );

        if (s2cellTokens.isEmpty()) {
            return MapObjectsResponse.empty();
        }

        // 2. 각 타입의 객체를 DB에서 조회합니다.
        List<Post> grains = postQueryRepository.findGrainsInCells(s2cellTokens);
        List<StaticCloud> staticClouds = staticCloudRepository.findCloudsInCells(s2cellTokens);
        List<DynamicCloud> dynamicClouds = dynamicCloudRepository.findActiveCloudsInCells(
            s2cellTokens);

        // 3. 조회된 객체들에 필요한 추가 정보를 조회합니다.
        Map<Long, Member> authors = getAuthors(grains);
        Map<Long, Long> staticCloudPostCounts = getStaticCloudPostCounts(staticClouds);
        Map<Long, Long> dynamicCloudPostCounts = getDynamicCloudPostCounts(dynamicClouds);

        // 4. 조회된 엔티티를 DTO로 변환합니다.
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
                staticCloudPostCounts.getOrDefault(cloud.getId(), 0L),
                s2PolygonConverter.convertS2TokensToPolygon(cloud.getS2TokenIds())
            ))
            .toList();

        List<MapObjectsResponse.DynamicCloudInfo> dynamicCloudDtos = dynamicClouds.stream()
            .map(cloud -> new MapObjectsResponse.DynamicCloudInfo(
                cloud.getId().toString(),
                dynamicCloudPostCounts.getOrDefault(cloud.getId(), 0L),
                s2PolygonConverter.convertS2TokensToPolygon(cloud.getS2TokenIds())
            ))
            .toList();

        return new MapObjectsResponse(grainDtos, staticCloudDtos, dynamicCloudDtos);
    }

    private Map<Long, Member> getAuthors(List<Post> grains) {
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

    private Map<Long, Long> getStaticCloudPostCounts(List<StaticCloud> clouds) {
        if (clouds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> cloudIds = clouds.stream().map(StaticCloud::getId).toList();
        return postQueryRepository.countPostsByStaticCloudIds(cloudIds);
    }

    private Map<Long, Long> getDynamicCloudPostCounts(List<DynamicCloud> clouds) {
        if (clouds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> cloudIds = clouds.stream().map(DynamicCloud::getId).toList();
        return postQueryRepository.countPostsByDynamicCloudIds(cloudIds);
    }
}

