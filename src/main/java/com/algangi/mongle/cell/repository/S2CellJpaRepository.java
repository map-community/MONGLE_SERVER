package com.algangi.mongle.cell.repository;

import com.algangi.mongle.cell.domain.S2Cell;
import com.algangi.mongle.place.domain.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface S2CellJpaRepository extends JpaRepository<S2Cell, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update S2Cell c set c.place = :place where c.s2CellId in :ids")
    int assignPlaceByCellIds(@Param("place") Place place, @Param("ids") List<String> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update S2Cell c set c.place = null where c.s2CellId in :ids")
    int unassignPlaceByCellIds(@Param("ids") List<String> ids);
}
