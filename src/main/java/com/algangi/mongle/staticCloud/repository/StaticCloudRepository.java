package com.algangi.mongle.staticCloud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algangi.mongle.staticCloud.domain.model.StaticCloud;

public interface StaticCloudRepository extends JpaRepository<StaticCloud, Long> {

    Optional<StaticCloud> findByName(String name);
}
