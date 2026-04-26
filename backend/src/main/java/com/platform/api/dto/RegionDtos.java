package com.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class RegionDtos {

    private RegionDtos() {}

    public record RegionDto(
            Long id,
            String code,
            String name,
            String countryCode,
            BigDecimal latitude,
            BigDecimal longitude,
            Long population,
            BigDecimal areaKm2,
            LocalDateTime createdAt
    ) {}

    public record CreateRegionRequest(
            @NotBlank @Size(max = 10) String code,
            @NotBlank String name,
            @NotBlank @Size(min = 2, max = 3) String countryCode,
            BigDecimal latitude,
            BigDecimal longitude,
            Long population,
            BigDecimal areaKm2
    ) {}

    public record UpdateRegionRequest(
            String name,
            String countryCode,
            BigDecimal latitude,
            BigDecimal longitude,
            Long population,
            BigDecimal areaKm2
    ) {}
}
