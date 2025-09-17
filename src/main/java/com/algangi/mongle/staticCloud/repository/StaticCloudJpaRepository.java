package com.algangi.mongle.staticCloud.repository;

import com.algangi.mongle.staticCloud.domain.StaticCloud;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaticCloudJpaRepository extends JpaRepository<StaticCloud, Long> {

    Optional<StaticCloud> findByName(String name);
}
