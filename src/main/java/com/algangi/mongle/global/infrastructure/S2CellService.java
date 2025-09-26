package com.algangi.mongle.global.infrastructure;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.algangi.mongle.global.domain.service.CellService;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;

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
}
