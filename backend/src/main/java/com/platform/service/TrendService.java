package com.platform.service;

import com.platform.api.dto.TrendDtos;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.EconomicIndicator;
import com.platform.domain.entity.IndicatorValue;
import com.platform.domain.entity.Region;
import com.platform.domain.entity.TrendModel;
import com.platform.domain.repository.IndicatorValueRepository;
import com.platform.domain.repository.TrendModelRepository;
import com.platform.service.exception.ResourceNotFoundException;
import com.platform.service.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final TrendModelRepository trendModelRepository;
    private final IndicatorValueRepository indicatorValueRepository;
    private final RegionService regionService;
    private final IndicatorService indicatorService;
    private final EntityMapper mapper;

    @Transactional
    public TrendDtos.TrendModelDto computeTrend(TrendDtos.TrendRequest request) {
        Region region = regionService.getOrThrow(request.regionId());
        EconomicIndicator indicator = indicatorService.getIndicatorOrThrow(request.indicatorId());

        List<IndicatorValue> values = indicatorValueRepository
                .findByRegionIdAndIndicatorIdOrderByYear(request.regionId(), request.indicatorId());

        if (values.size() < 2) {
            throw new ValidationException("At least 2 data points required for trend analysis");
        }

        double[] years = values.stream().mapToDouble(v -> v.getYear()).toArray();
        double[] vals = values.stream().mapToDouble(v -> v.getValue().doubleValue()).toArray();

        LinearRegressionResult lr = linearRegression(years, vals);
        double forecast = lr.slope() * request.forecastYear() + lr.intercept();

        TrendModel model = trendModelRepository
                .findByRegionIdAndIndicatorIdAndForecastYear(request.regionId(), request.indicatorId(), request.forecastYear())
                .orElse(TrendModel.builder().region(region).indicator(indicator).build());

        model.setModelType("LINEAR");
        model.setSlope(BigDecimal.valueOf(lr.slope()).setScale(8, RoundingMode.HALF_UP));
        model.setIntercept(BigDecimal.valueOf(lr.intercept()).setScale(8, RoundingMode.HALF_UP));
        model.setRSquared(BigDecimal.valueOf(lr.rSquared()).setScale(6, RoundingMode.HALF_UP));
        model.setForecastYear(request.forecastYear());
        model.setForecastValue(BigDecimal.valueOf(forecast).setScale(4, RoundingMode.HALF_UP));
        model.setParameters(Map.of("dataPoints", values.size(), "yearRange",
                List.of((int) years[0], (int) years[years.length - 1])));

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

    private LinearRegressionResult linearRegression(double[] x, double[] y) {
        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i]; sumY += y[i];
            sumXY += x[i] * y[i]; sumX2 += x[i] * x[i];
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        double meanY = sumY / n;
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < n; i++) {
            ssTot += Math.pow(y[i] - meanY, 2);
            ssRes += Math.pow(y[i] - (slope * x[i] + intercept), 2);
        }
        double rSquared = ssTot == 0 ? 1.0 : 1.0 - ssRes / ssTot;
        return new LinearRegressionResult(slope, intercept, rSquared);
    }

    private record LinearRegressionResult(double slope, double intercept, double rSquared) {}
}
