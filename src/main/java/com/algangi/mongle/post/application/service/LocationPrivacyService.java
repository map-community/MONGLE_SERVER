package com.algangi.mongle.post.application.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.algangi.mongle.global.domain.service.CellService;
import com.algangi.mongle.post.application.dto.LocationDeterminationResult;
import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationPrivacyService {

    private static final double RANDOM_RADIUS_METERS = 15.0;
    private static final int S2_CELL_LEVEL = 19;
    private static final double EARTH_RADIUS_METERS = 6371000.0;
    private final StaticCloudRepository staticCloudRepository;
    private final CellService cellService;

    public LocationDeterminationResult determineFinalS2Token(Location location,
        boolean isRandomLocationEnabled) {
        String originalS2TokenId = cellService.generateS2TokenIdFrom(location.getLatitude(),
            location.getLongitude());

        String finalS2TokenId;
        if (!isRandomLocationEnabled || staticCloudRepository.findByS2TokenId(
            originalS2TokenId).isPresent()) {
            finalS2TokenId = originalS2TokenId;
        } else {
            finalS2TokenId = randomizeLocation(originalS2TokenId);
        }

        Location finalLocation = cellService.getLocationFrom(finalS2TokenId);
        return LocationDeterminationResult.of(finalS2TokenId, finalLocation);
    }

    private String randomizeLocation(String s2Token) {
        S2CellId cellId = S2CellId.fromToken(s2Token);
        S2LatLng originalLatLng = cellId.toLatLng();

        S2LatLng randomLatLng = calculateRandomLatLng(originalLatLng, RANDOM_RADIUS_METERS);

        return S2CellId.fromLatLng(randomLatLng).parent(S2_CELL_LEVEL).toToken();
    }


    private S2LatLng calculateRandomLatLng(S2LatLng originalLatLng, double maxRadiusMeters) {
        double randomBearing = ThreadLocalRandom.current()
            .nextDouble(0, 2 * Math.PI);

        double randomDistance =
            Math.sqrt(ThreadLocalRandom.current().nextDouble()) * maxRadiusMeters;

        double angularDistance = randomDistance / EARTH_RADIUS_METERS;

        double lat1 = originalLatLng.latRadians();
        double lon1 = originalLatLng.lngRadians();

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDistance) +
            Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(randomBearing));

        double lon2 =
            lon1 + Math.atan2(Math.sin(randomBearing) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));

        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return S2LatLng.fromRadians(lat2, lon2);
    }
}
