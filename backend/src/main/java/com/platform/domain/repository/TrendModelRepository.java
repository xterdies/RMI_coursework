package com.platform.domain.repository;

import com.platform.domain.entity.TrendModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrendModelRepository extends JpaRepository<TrendModel, Long> {
    List<TrendModel> findByRegionIdAndIndicatorId(Long regionId, Long indicatorId);
    Optional<TrendModel> findByRegionIdAndIndicatorIdAndForecastYear(Long regionId, Long indicatorId, Integer forecastYear);
    List<TrendModel> findByRegionId(Long regionId);
}
