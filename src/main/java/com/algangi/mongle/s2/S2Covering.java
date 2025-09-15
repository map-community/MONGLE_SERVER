package com.algangi.mongle.s2;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2Region;
import com.google.common.geometry.S2RegionCoverer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class S2Covering {

    private S2Covering() {
    }

    public static S2Loop ringToLoop(List<double[]> ringLngLat) {
        if (ringLngLat == null || ringLngLat.size() < 3) {
            throw new IllegalArgumentException("Ring must have at least 3 vertices");
        }
        List<S2Point> pts = new ArrayList<>(ringLngLat.size());
        for (double[] p : ringLngLat) {
            if (p == null || p.length < 2) {
                throw new IllegalArgumentException("Each coordinate must be [lng, lat]");
            }
            double lng = p[0];
            double lat = p[1];
            S2LatLng ll = S2LatLng.fromDegrees(lat, lng);
            pts.add(ll.toPoint());
        }
        S2Loop loop = new S2Loop(pts);
        loop.normalize();
        return loop;
    }

    public static S2Polygon polygon(List<List<double[]>> rings) {
        if (rings == null || rings.isEmpty()) {
            throw new IllegalArgumentException("Polygon rings must not be empty");
        }
        List<S2Loop> loops = new ArrayList<>(rings.size());
        for (List<double[]> ring : rings) {
            loops.add(ringToLoop(ring));
        }
        return new S2Polygon(loops);
    }

    public static S2Polygon multiPolygon(List<List<List<double[]>>> multipoly) {
        if (multipoly == null || multipoly.isEmpty()) {
            throw new IllegalArgumentException("MultiPolygon must not be empty");
        }
        S2Polygon acc = new S2Polygon();
        for (List<List<double[]>> poly : multipoly) {
            S2Polygon p = polygon(poly);
            S2Polygon result = new S2Polygon();
            result.initToUnion(acc, p);
            acc = result;
        }
        return acc;
    }

    @SuppressWarnings("unchecked")
    public static S2Polygon toS2PolygonFromGeometry(Object geometry) {
        Map<String, Object> g = (Map<String, Object>) geometry;
        String type = (String) g.get("type");
        Object coords = g.get("coordinates");
        if ("Polygon".equals(type)) {
            return polygon(GeoJsonLoader.asPolygon(coords));
        } else if ("MultiPolygon".equals(type)) {
            return multiPolygon(GeoJsonLoader.asMultiPolygon(coords));
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    public static S2Polygon unionOfFeatureCollection(Map<String, Object> featureCollection) {
        List<Map<String, Object>> features = (List<Map<String, Object>>) featureCollection.get(
            "features");
        if (features == null || features.isEmpty()) {
            throw new IllegalArgumentException("FeatureCollection has no features");
        }
        S2Polygon acc = new S2Polygon();
        for (Map<String, Object> f : features) {
            Map<String, Object> geom = (Map<String, Object>) f.get("geometry");
            S2Polygon p = toS2PolygonFromGeometry(geom);
            S2Polygon result = new S2Polygon();
            result.initToUnion(acc, p);
            acc = result;
        }
        return acc;
    }

    public static List<String> coverAtLevel(S2Region region, int level) {
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(level);
        coverer.setMaxLevel(level);
        ArrayList<S2CellId> covering = new ArrayList<>();
        coverer.getCovering(region, covering);
        return covering.stream().map(S2CellId::toToken).collect(Collectors.toList());
    }

    public static List<String> coverLevel19(S2Region region) {
        return coverAtLevel(region, 19);
    }
}
