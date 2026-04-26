package com.platform.service;

import com.platform.api.dto.TrendDtos;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.EconomicIndicator;
import com.platform.domain.entity.IndicatorValue;
import com.platform.domain.entity.Region;
import com.platform.domain.entity.TrendModel;
import com.platform.domain.repository.IndicatorValueRepository;
import com.platform.domain.repository.TrendModelRepository;
import com.platform.service.trend.TrendComputationResult;
import com.platform.service.trend.TrendComputationStrategy;
import com.platform.service.exception.ResourceNotFoundException;
import com.platform.service.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final TrendModelRepository trendModelRepository;
    private final IndicatorValueRepository indicatorValueRepository;
    private final RegionService regionService;
    private final IndicatorService indicatorService;
    private final EntityMapper mapper;
    private final TrendComputationStrategy trendStrategy;

    @Transactional
    public TrendDtos.TrendModelDto computeTrend(TrendDtos.TrendRequest request) {
        Region region = regionService.getOrThrow(request.regionId());
        EconomicIndicator indicator = indicatorService.getIndicatorOrThrow(request.indicatorId());

        List<IndicatorValue> values = indicatorValueRepository
                .findByRegionIdAndIndicatorIdOrderByYear(request.regionId(), request.indicatorId());

        if (values.size() < 2) {
            throw new ValidationException("At least 2 data points required for trend analysis");
        }

        // Design Pattern: Strategy - swap trend computation algorithms without changing service orchestration.
        TrendComputationResult computed = trendStrategy.compute(values, request.forecastYear());

        TrendModel model = trendModelRepository
                .findByRegionIdAndIndicatorIdAndForecastYear(request.regionId(), request.indicatorId(), request.forecastYear())
                .orElse(TrendModel.builder().region(region).indicator(indicator).build());

        model.setModelType(trendStrategy.modelType());
        model.setSlope(computed.slope());
        model.setIntercept(computed.intercept());
        model.setRSquared(computed.rSquared());
        model.setForecastYear(request.forecastYear());
        model.setForecastValue(computed.forecastValue());
        model.setParameters(computed.parameters());

        return mapper.toTrendModelDto(trendModelRepository.save(model));
    }

    @Transactional(readOnly = true)
    public List<TrendDtos.TrendModelDto> findByRegion(Long regionId) {
        if (!regionService.findById(regionId).id().equals(regionId)) {
            throw new ResourceNotFoundException("Region not found: " + regionId);
        }
        return trendModelRepository.findByRegionId(regionId).stream()
                .map(mapper::toTrendModelDto).toList();
    }
}
