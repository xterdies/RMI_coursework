package com.platform.service.trend;

import java.math.BigDecimal;
import java.util.Map;

public record TrendComputationResult(
        BigDecimal slope,
        BigDecimal intercept,
        BigDecimal rSquared,
        BigDecimal forecastValue,
        Map<String, Object> parameters
) {
}

