package com.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class IndicatorDtos {

    private IndicatorDtos() {}

    public record IndicatorDto(
            Long id,
            String code,
            String name,
            String description,
            String unit,
            String source,
            String worldBankCode,
            LocalDateTime createdAt
    ) {}

    public record CreateIndicatorRequest(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            String unit,
            String source,
            String worldBankCode
    ) {}

    public record IndicatorValueDto(
            Long id,
            Long regionId,
            String regionName,
            Long indicatorId,
            String indicatorName,
            Integer year,
            BigDecimal value,
            String sourceUrl
    ) {}

    public record CreateIndicatorValueRequest(
            @NotNull Long regionId,
            @NotNull Long indicatorId,
            @NotNull Integer year,
            @NotNull BigDecimal value,
            String sourceUrl
    ) {}
}
