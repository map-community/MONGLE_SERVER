package com.algangi.mongle.global.util;

import com.algangi.mongle.map.presentation.dto.MapObjectsResponse;
import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2PolygonBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class S2PolygonConverter {

    /**
     * S2 토큰 Set을 받아 외부 경계를 나타내는 좌표 List로 변환합니다.
     *
     * @param s2Tokens S2 Cell ID 토큰 Set
     * @return 경계 좌표 List
     */
    public List<MapObjectsResponse.Coordinate> convertS2TokensToPolygon(Set<String> s2Tokens) {
        if (s2Tokens == null || s2Tokens.isEmpty()) {
            return new ArrayList<>();
        }

        S2PolygonBuilder builder = new S2PolygonBuilder(S2PolygonBuilder.Options.DIRECTED_XOR);
        for (String token : s2Tokens) {
            S2CellId cellId = S2CellId.fromToken(token);
            S2Loop loop = new S2Loop(new S2Cell(cellId));
            builder.addLoop(loop);
        }

        S2Polygon polygon = new S2Polygon();
        builder.assemblePolygon(polygon, null);

        if (polygon.numLoops() == 0) {
            return new ArrayList<>();
        }

        S2Loop outerLoop = polygon.loop(0);
        List<S2Point> vertices = new ArrayList<>();
        for (int i = 0; i < outerLoop.numVertices(); i++) {
            vertices.add(outerLoop.vertex(i));
        }

        return vertices.stream()
            .map(s2Point -> {
                S2LatLng latLng = new S2LatLng(s2Point);
                return new MapObjectsResponse.Coordinate(latLng.latDegrees(), latLng.lngDegrees());
            })
            .collect(Collectors.toList());
    }
}

