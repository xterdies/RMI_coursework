package com.platform.service.trend;

import com.platform.domain.entity.IndicatorValue;

import java.util.List;

public interface TrendComputationStrategy {
    TrendComputationResult compute(List<IndicatorValue> values, int forecastYear);
    String modelType();
}

