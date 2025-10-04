package com.algangi.mongle.batch;

import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mongle.batch.manual-s2seed.enabled", havingValue = "true")
@Slf4j
public class ManualS2SeedRunner implements CommandLineRunner {

    private final StaticCloudRepository staticCloudRepo;
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 500;
    private static final String STATIC_DEFS_CSV = "s2/static_cloud_defs.csv";

    @Override
    @Transactional
    public void run(String... args) {
        log.info("ManualS2Seed START for Static Clouds");
        Map<String, CloudDef> staticDefs = loadStaticDefsFromCsv(STATIC_DEFS_CSV);
        upsertStaticByName(staticDefs);
        log.info("ManualS2Seed DONE for Static Clouds");
    }

    private void upsertStaticByName(Map<String, CloudDef> staticDefs) {
        final String sql = """
            INSERT INTO static_cloud_s2_cell (s2_token_id, cloud_id)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE cloud_id = VALUES(cloud_id)
            """;

        int totalAffected = 0;

        for (Map.Entry<String, CloudDef> e : staticDefs.entrySet()) {
            String name = e.getKey();
            CloudDef def = e.getValue();

            StaticCloud sc = staticCloudRepo.findByName(name)
                .orElseGet(() -> createStaticCloudSafely(name, def.lat, def.lng));

            List<String> tokens = def.tokens;

            for (List<String> batch : partition(tokens, BATCH_SIZE)) {
                List<Object[]> params = batch.stream()
                    .map(t -> new Object[]{t, sc.getId()})
                    .collect(Collectors.toList());
                int[] res = jdbcTemplate.batchUpdate(sql, params);
                totalAffected += Arrays.stream(res).sum();
            }

            log.info("ManualS2Seed STATIC name={}, cloud_id={}, tokens={}", name, sc.getId(),
                tokens.size());
        }

        log.info("ManualS2Seed STATIC affectedRows~={}", totalAffected);
    }

    private StaticCloud createStaticCloudSafely(String name, Double lat, Double lng) {
        try {
            return staticCloudRepo.save(StaticCloud.createStaticCloud(name, lat, lng, Set.of()));
        } catch (DataIntegrityViolationException ex) {
            return staticCloudRepo.findByName(name).orElseThrow(() -> ex);
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        if (list.isEmpty()) {
            return List.of();
        }
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            batches.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return batches;
    }

    private static Map<String, CloudDef> loadStaticDefsFromCsv(String path) {
        Map<String, CloudDef> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
            new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",", -1);
                if (arr.length < 4) {
                    continue;
                }
                String name = arr[0].trim();
                double lat = Double.parseDouble(arr[1].trim());
                double lng = Double.parseDouble(arr[2].trim());
                String token = arr[3].trim();
                CloudDef def = map.computeIfAbsent(name,
                    k -> new CloudDef(lat, lng, new ArrayList<>()));
                def.tokens.add(token);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + path, e);
        }
        return map;
    }

    private static class CloudDef {

        final double lat;
        final double lng;
        final List<String> tokens;

        CloudDef(double lat, double lng, List<String> tokens) {
            this.lat = lat;
            this.lng = lng;
            this.tokens = tokens;
        }
    }
}