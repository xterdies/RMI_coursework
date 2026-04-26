package com.platform.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public final class TrendDtos {

    private TrendDtos() {}

    public record TrendRequest(
            @NotNull Long regionId,
            @NotNull Long indicatorId,
            @NotNull Integer forecastYear
    ) {}

    public record TrendModelDto(
            Long id,
            Long regionId,
            String regionName,
            Long indicatorId,
            String indicatorName,
            String modelType,
            BigDecimal slope,
            BigDecimal intercept,
            BigDecimal rSquared,
            Integer forecastYear,
            BigDecimal forecastValue,
            Map<String, Object> parameters,
            LocalDateTime createdAt
    ) {}
}
