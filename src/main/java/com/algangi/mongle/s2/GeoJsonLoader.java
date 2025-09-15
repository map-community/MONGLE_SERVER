package com.algangi.mongle.s2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GeoJsonLoader {

    private static final ObjectMapper om = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE =
        new TypeReference<Map<String, Object>>() {
        };

    private GeoJsonLoader() {
    }

    public static Map<String, Object> loadFromClasspath(String path) {
        try (InputStream is = GeoJsonLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Not found: " + path);
            }
            return om.readValue(is, MAP_TYPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<double[]> toLngLatRing(List<List<Double>> ring) {
        List<double[]> out = new ArrayList<>(ring.size());
        for (List<Double> p : ring) {
            out.add(new double[]{p.get(0), p.get(1)});
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static List<List<double[]>> asPolygon(Object coords) {
        List<List<List<Double>>> raw = (List<List<List<Double>>>) coords;
        List<List<double[]>> rings = new ArrayList<>(raw.size());
        for (List<List<Double>> ring : raw) {
            rings.add(toLngLatRing(ring));
        }
        return rings;
    }

    @SuppressWarnings("unchecked")
    public static List<List<List<double[]>>> asMultiPolygon(Object coords) {
        List<List<List<List<Double>>>> raw = (List<List<List<List<Double>>>>) coords;
        List<List<List<double[]>>> polys = new ArrayList<>(raw.size());
        for (List<List<List<Double>>> poly : raw) {
            List<List<double[]>> rings = new ArrayList<>(poly.size());
            for (List<List<Double>> ring : poly) {
                rings.add(toLngLatRing(ring));
            }
            polys.add(rings);
        }
        return polys;
    }
}
