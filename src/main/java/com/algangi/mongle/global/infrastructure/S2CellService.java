package com.algangi.mongle.global.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.algangi.mongle.global.domain.service.CellService;
import com.algangi.mongle.post.domain.model.Location;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;
import com.google.common.geometry.S2RegionCoverer;

@Service
public class S2CellService implements CellService {

    private static final int S2_CELL_LEVEL = 19;

    @Override
    public String generateS2TokenIdFrom(double latitude, double longitude) {
        S2LatLng latLng = S2LatLng.fromDegrees(latitude, longitude);
        S2CellId cellId = S2CellId.fromLatLng(latLng);
        S2CellId parentCellId = cellId.parent(S2_CELL_LEVEL);
        return parentCellId.toToken();
    }

    @Override
    public Set<String> getAdjacentCells(String s2TokenId) {
        S2CellId cellId = S2CellId.fromToken(s2TokenId);

        ArrayList<S2CellId> neighbors = new ArrayList<>();
        cellId.getAllNeighbors(cellId.level(), neighbors);

        return neighbors.stream()
            .map(S2CellId::toToken)
            .collect(Collectors.toSet());
    }

    @Override
    public Location getLocationFrom(String s2TokenId) {
        S2CellId cellId = S2CellId.fromToken(s2TokenId);
        S2LatLng latLng = cellId.toLatLng();
        return Location.create(latLng.latDegrees(), latLng.lngDegrees());
    }

    public List<String> getCellsForRect(double swLat, double swLng, double neLat, double neLng) {
        S2LatLngRect rect = S2LatLngRect.fromPointPair(
            S2LatLng.fromDegrees(swLat, swLng),
            S2LatLng.fromDegrees(neLat, neLng)
        );

        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(S2_CELL_LEVEL);
        coverer.setMaxLevel(S2_CELL_LEVEL);

        ArrayList<S2CellId> covering = new ArrayList<>();
        coverer.getCovering(rect, covering);

        return covering.stream()
            .map(S2CellId::toToken)
            .collect(Collectors.toList());
    }
}

