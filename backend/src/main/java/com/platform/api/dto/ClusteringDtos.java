package com.platform.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ClusteringDtos {

    private ClusteringDtos() {}

    public record ClusteringRequest(
            @NotBlank String name,
            @NotNull @Min(2) @Max(20) Integer kClusters,
            @NotNull Integer year,
            @NotNull List<Long> indicatorIds
    ) {}

    public record ClusteringRunDto(
            Long id,
            String name,
            Integer kClusters,
            Integer year,
            String algorithm,
            Integer iterations,
            BigDecimal inertia,
            Map<String, Object> metadata,
            LocalDateTime createdAt,
            List<AssignmentDto> assignments
    ) {}

    public record AssignmentDto(
            Long regionId,
            String regionName,
            String regionCode,
            Integer clusterLabel,
            BigDecimal distanceToCentroid
    ) {}
}
