package com.algangi.mongle.global.infrastructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.algangi.mongle.global.domain.service.CellService;
import com.google.common.geometry.S2CellId;

@Service
public class S2CellService implements CellService {

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
