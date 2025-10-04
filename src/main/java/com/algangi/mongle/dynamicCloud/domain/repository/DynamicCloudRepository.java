package com.algangi.mongle.dynamicCloud.domain.repository;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DynamicCloudRepository extends JpaRepository<DynamicCloud, Long> {

    @Query("select dc from DynamicCloud dc join dc.s2TokenIds s2TokenId " +
        "where s2TokenId = :s2TokenId and dc.status = 'ACTIVE'")
    Optional<DynamicCloud> findActiveByS2TokenId(@Param("s2TokenId") String s2TokenId);

    // 주어진 S2 Cell 목록과 겹치는 활성화된 동적 구름들을 조회
    @Query("SELECT DISTINCT dc FROM DynamicCloud dc JOIN FETCH dc.s2TokenIds WHERE dc.id IN " +
        "(SELECT d.id FROM DynamicCloud d JOIN d.s2TokenIds s2_token WHERE s2_token IN :s2cellTokens AND d.status = 'ACTIVE')")
    List<DynamicCloud> findActiveCloudsInCells(@Param("s2cellTokens") List<String> s2cellTokens);

}