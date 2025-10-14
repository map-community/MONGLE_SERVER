package com.algangi.mongle.post.domain.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.algangi.mongle.post.domain.model.Location;
import com.google.common.geometry.S2LatLng;

@Service
public class LocationRandomizer {

    private static final double RANDOM_RADIUS_METERS = 15.0;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    public Location randomize(Location originalLocation) {
        S2LatLng originalLatLng = S2LatLng.fromDegrees(originalLocation.getLatitude(),
            originalLocation.getLongitude());
        S2LatLng randomLatLng = calculateRandomLatLng(originalLatLng);
        return Location.create(randomLatLng.latDegrees(), randomLatLng.lngDegrees());
    }

    private S2LatLng calculateRandomLatLng(S2LatLng originalLatLng) {
        double randomBearing = ThreadLocalRandom.current()
            .nextDouble(0, 2 * Math.PI);

        double randomDistance =
            Math.sqrt(ThreadLocalRandom.current().nextDouble()) * RANDOM_RADIUS_METERS;

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
