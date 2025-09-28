package com.algangi.mongle.staticCloud.repository;

import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaticCloudRepository extends JpaRepository<StaticCloud, Long> {

    Optional<StaticCloud> findByName(String name);

    @Query("select sc from StaticCloud sc join sc.s2TokenIds s2TokenId where s2TokenId = :s2TokenId")
    Optional<StaticCloud> findByS2TokenId(@Param("s2TokenId") String s2TokenId);

    /**
     * 주어진 S2 Cell 목록과 겹치는 정적 구름을 모두 조회합니다.
     *
     * @param s2TokenIds S2 Cell 토큰 ID 목록
     * @return 정적 구름(StaticCloud) 목록
     */
    @Query("select DISTINCT sc from StaticCloud sc join sc.s2TokenIds s2TokenId where s2TokenId in :s2TokenIds")
    List<StaticCloud> findCloudsInCells(@Param("s2TokenIds") List<String> s2TokenIds);
}

