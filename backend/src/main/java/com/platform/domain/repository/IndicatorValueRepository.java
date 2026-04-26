package com.platform.domain.repository;

import com.platform.domain.entity.IndicatorValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IndicatorValueRepository extends JpaRepository<IndicatorValue, Long> {

    List<IndicatorValue> findByRegionIdAndIndicatorIdOrderByYear(Long regionId, Long indicatorId);

    List<IndicatorValue> findByRegionIdAndYear(Long regionId, Integer year);

    @Query("SELECT iv FROM IndicatorValue iv WHERE iv.indicator.id = :indicatorId AND iv.year = :year")
    List<IndicatorValue> findByIndicatorIdAndYear(@Param("indicatorId") Long indicatorId,
                                                   @Param("year") Integer year);

    Optional<IndicatorValue> findByRegionIdAndIndicatorIdAndYear(Long regionId, Long indicatorId, Integer year);

    @Query("SELECT DISTINCT iv.year FROM IndicatorValue iv WHERE iv.region.id = :regionId ORDER BY iv.year")
    List<Integer> findDistinctYearsByRegionId(@Param("regionId") Long regionId);
}
