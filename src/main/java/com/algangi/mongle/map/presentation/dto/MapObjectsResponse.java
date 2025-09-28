package com.algangi.mongle.map.presentation.dto;

import java.util.Collections;
import java.util.List;

public record MapObjectsResponse(
    List<Grain> grains,
    List<StaticCloudInfo> staticClouds,
    List<DynamicCloudInfo> dynamicClouds
) {

    public record Grain(
        String postId,
        double latitude,
        double longitude,
        String profileImageUrl
    ) {

    }

    public record StaticCloudInfo(
        String placeId,
        String name,
        double centerLatitude,
        double centerLongitude,
        long postCount,
        List<Coordinate> polygon
    ) {

    }

    public record DynamicCloudInfo(
        String cloudId,
        long postCount,
        List<Coordinate> polygon
    ) {

    }

    public record Coordinate(
        double latitude,
        double longitude
    ) {

    }

    public static MapObjectsResponse empty() {
        return new MapObjectsResponse(Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList());
    }
}

