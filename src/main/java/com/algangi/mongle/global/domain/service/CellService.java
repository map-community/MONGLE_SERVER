package com.algangi.mongle.global.domain.service;

import java.util.Set;

public interface CellService {

    public Set<String> getAdjacentCells(String s2TokenId);

    String generateS2TokenIdFrom(double latitude, double longitude);

}
