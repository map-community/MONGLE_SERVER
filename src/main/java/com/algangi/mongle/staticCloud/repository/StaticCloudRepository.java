package com.algangi.mongle.staticCloud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.algangi.mongle.staticCloud.domain.model.StaticCloud;

public interface StaticCloudRepository extends JpaRepository<StaticCloud, Long> {

    Optional<StaticCloud> findByName(String name);

    @Query("select sc from StaticCloud sc join sc.s2TokenIds s2TokenId where s2TokenId = :s2TokenId")
    Optional<StaticCloud> findByS2TokenId(@Param("s2TokenId") String s2TokenId);
}
