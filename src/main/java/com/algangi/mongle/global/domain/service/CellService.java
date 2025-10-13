package com.algangi.mongle.global.domain.service;

import java.util.Set;

import com.algangi.mongle.post.domain.model.Location;

public interface CellService {

    public Set<String> getAdjacentCells(String s2TokenId);

    String generateS2TokenIdFrom(double latitude, double longitude);

    Location getLocationFrom(String s2TokenId);

}
