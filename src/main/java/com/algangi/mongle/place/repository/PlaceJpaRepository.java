package com.algangi.mongle.place.repository;

import com.algangi.mongle.place.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {

}
