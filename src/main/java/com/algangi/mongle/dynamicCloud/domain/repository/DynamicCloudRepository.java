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

    /**
     * 주어진 S2 Cell 목록과 겹치는 활성 상태의 동적 구름을 모두 조회합니다.
     *
     * @param s2TokenIds S2 Cell 토큰 ID 목록
     * @return 활성 상태의 동적 구름(DynamicCloud) 목록
     */
    @Query("select DISTINCT dc from DynamicCloud dc join dc.s2TokenIds s2TokenId " +
        "where s2TokenId in :s2TokenIds and dc.status = 'ACTIVE'")
    List<DynamicCloud> findActiveCloudsInCells(@Param("s2TokenIds") List<String> s2TokenIds);

}
