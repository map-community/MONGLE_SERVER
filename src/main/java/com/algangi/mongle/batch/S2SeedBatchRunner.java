package com.algangi.mongle.batch;

import com.algangi.mongle.place.domain.Place;
import com.algangi.mongle.place.repository.PlaceJpaRepository;
import com.algangi.mongle.s2.GeoJsonLoader;
import com.algangi.mongle.s2.S2Covering;
import com.algangi.mongle.cell.repository.S2CellJpaRepository;
import com.google.common.geometry.S2Polygon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mongle.batch.s2seed.enabled", havingValue = "true")
@Slf4j
public class S2SeedBatchRunner implements CommandLineRunner {

    private final PlaceJpaRepository placeJpaRepository;
    private final S2CellJpaRepository s2CellJpaRepository;

    @PersistenceContext
    private EntityManager em;

    private static final String CAMPUS_BOUNDARY_PATH = "/data/knu/campus_boundary.geojson";
    private static final String PLACES_PATH = "/data/knu/places.geojson";
    private static final int UPSERT_CHUNK = 1000;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[S2Seed] START");
        seedCampusL19Cells();
        mapPlacesToCells();
        log.info("[S2Seed] DONE");
    }

    private void seedCampusL19Cells() {
        Map<String, Object> fc = GeoJsonLoader.loadFromClasspath(CAMPUS_BOUNDARY_PATH);
        S2Polygon campus = S2Covering.unionOfFeatureCollection(fc);
        List<String> tokens = S2Covering.coverLevel19(campus);

        String sql =
            "INSERT INTO s2_cell (s2cell_id, dynamic_cloud_id, place_id) " +
                "VALUES (?, NULL, NULL) " +
                "ON DUPLICATE KEY UPDATE s2cell_id = s2cell_id";

        int total = tokens.size();
        int affected = 0;

        for (int i = 0; i < total; i++) {
            em.createNativeQuery(sql)
                .setParameter(1, tokens.get(i))
                .executeUpdate();
            if ((i + 1) % UPSERT_CHUNK == 0) {
                em.flush();
                em.clear();
            }
            affected++;
        }
        em.flush();
        em.clear();

        log.info("[S2Seed] s2_cell upsert: tokens={}, affected={}", total, affected);
    }

    private void mapPlacesToCells() {
        Map<String, Object> fc = GeoJsonLoader.loadFromClasspath(PLACES_PATH);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> features = (List<Map<String, Object>>) fc.get("features");

        List<PlaceCover> covers = new ArrayList<>();

        for (Map<String, Object> f : features) {
            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) f.get("properties");

            double centerLat = ((Number) props.get("center_lat")).doubleValue();
            double centerLng = ((Number) props.get("center_lng")).doubleValue();
            Integer priority =
                props.containsKey("priority") ? ((Number) props.get("priority")).intValue() : null;

            Place place = placeJpaRepository.save(
                Place.createPlace(centerLat, centerLng, List.of()));

            @SuppressWarnings("unchecked")
            Map<String, Object> geom = (Map<String, Object>) f.get("geometry");
            S2Polygon p = S2Covering.toS2PolygonFromGeometry(geom);
            List<String> tokens = S2Covering.coverLevel19(p);
            double area = p.getArea();

            covers.add(new PlaceCover(place, tokens, area, priority, centerLat, centerLng));
        }

        covers.sort(
            Comparator.<PlaceCover, Integer>comparing(pc -> pc.priority,
                    Comparator.nullsLast(Integer::compareTo))
                .thenComparingDouble(pc -> pc.area)
        );

        Set<String> assignedInRun = new HashSet<>();
        for (PlaceCover pc : covers) {
            List<String> candidate = pc.tokens.stream()
                .filter(t -> !assignedInRun.contains(t))
                .collect(Collectors.toList());
            if (candidate.isEmpty()) {
                continue;
            }
            int updated = s2CellJpaRepository.assignPlaceIfNullByCellIds(pc.place, candidate);
            if (updated > 0) {
                assignedInRun.addAll(candidate);
            }
            log.info("[S2Seed] place_id={} mapped cells={}", pc.place, updated);
        }
    }

    private record PlaceCover(Place place, List<String> tokens, double area, Integer priority,
                              double centerLat, double centerLng) {

    }
}
