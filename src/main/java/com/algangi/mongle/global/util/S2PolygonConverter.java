package com.algangi.mongle.global.util;

import com.algangi.mongle.map.presentation.dto.MapObjectsResponse;
import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2PolygonBuilder;
import com.google.common.geometry.S2Loop;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class S2PolygonConverter {

    /**
     * S2 Cell ID 집합으로부터 외부 경계 폴리곤을 생성합니다. 프론트엔드에서 폴리곤을 그릴 수 있도록 마지막 좌표는 첫 좌표와 동일하게 설정됩니다 (Closed
     * Polygon).
     *
     * @param s2Tokens S2 Cell 토큰 ID 집합
     * @return 폴리곤의 꼭짓점 좌표 리스트
     */
    public List<MapObjectsResponse.Coordinate> convert(Set<String> s2Tokens) {
        if (s2Tokens == null || s2Tokens.isEmpty()) {
            return new ArrayList<>();
        }

        S2PolygonBuilder builder = new S2PolygonBuilder(S2PolygonBuilder.Options.DIRECTED_XOR);
        for (String token : s2Tokens) {
            S2CellId cellId = S2CellId.fromToken(token);
            builder.addLoop(new S2Loop(new S2Cell(cellId)));
        }

        S2Polygon polygon = new S2Polygon();
        if (!builder.assemblePolygon(polygon, null) || polygon.numLoops() == 0) {
            return new ArrayList<>();
        }

        S2Loop outerLoop = polygon.loop(0);
        List<MapObjectsResponse.Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < outerLoop.numVertices(); i++) {
            S2Point vertex = outerLoop.vertex(i);
            S2LatLng latLng = new S2LatLng(vertex);
            coordinates.add(
                new MapObjectsResponse.Coordinate(latLng.latDegrees(), latLng.lngDegrees()));
        }

        if (!coordinates.isEmpty()) {
            coordinates.add(coordinates.getFirst());
        }

        return coordinates;
    }
}

