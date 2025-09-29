package com.algangi.mongle.staticCloud.repository;

import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaticCloudRepository extends JpaRepository<StaticCloud, Long> {

    Optional<StaticCloud> findByName(String name);

    @Query("select sc from StaticCloud sc join sc.s2TokenIds s2TokenId where s2TokenId = :s2TokenId")
    Optional<StaticCloud> findByS2TokenId(@Param("s2TokenId") String s2TokenId);

    // 주어진 S2 Cell 목록과 겹치는 정적 구름들을 조회
    @Query("select distinct sc from StaticCloud sc join sc.s2TokenIds s2TokenId where s2TokenId in :s2cellTokens")
    List<StaticCloud> findCloudsInCells(@Param("s2cellTokens") List<String> s2cellTokens);
}
