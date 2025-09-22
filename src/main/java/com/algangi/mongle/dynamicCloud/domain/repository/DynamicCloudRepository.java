package com.algangi.mongle.dynamicCloud.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;

public interface DynamicCloudRepository extends JpaRepository<DynamicCloud, Long> {

    @Query("select dc from DynamicCloud dc join dc.s2TokenIds s2TokenId " +
        "where s2TokenId = :s2TokenId and dc.status = 'ACTIVE'")
    Optional<DynamicCloud> findActiveByS2TokenId(@Param("s2TokenId") String s2TokenId);

    @Query("select DISTINCT dc from DynamicCloud dc join dc.s2TokenIds s2TokenId " +
        "where s2TokenId in :s2TokenIds and dc.status = 'ACTIVE'")
    List<DynamicCloud> findActiveCloudsInCells(@Param("s2TokenIds") Set<String> s2TokenIds);

}
