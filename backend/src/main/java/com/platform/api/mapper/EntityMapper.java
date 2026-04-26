package com.platform.api.mapper;

import com.platform.api.dto.*;
import com.platform.domain.entity.*;
import org.mapstruct.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntityMapper {

    // User
    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    UserDto toUserDto(User user);

    // Region
    RegionDtos.RegionDto toRegionDto(Region region);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Region toRegion(RegionDtos.CreateRegionRequest request);

    // Indicator
    IndicatorDtos.IndicatorDto toIndicatorDto(EconomicIndicator indicator);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    EconomicIndicator toIndicator(IndicatorDtos.CreateIndicatorRequest request);

    // IndicatorValue
    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "indicatorId", source = "indicator.id")
    @Mapping(target = "indicatorName", source = "indicator.name")
    IndicatorDtos.IndicatorValueDto toIndicatorValueDto(IndicatorValue value);

    // ClusteringAssignment
    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "regionCode", source = "region.code")
    ClusteringDtos.AssignmentDto toAssignmentDto(ClusteringAssignment assignment);

    // ClusteringRun
    default ClusteringDtos.ClusteringRunDto toClusteringRunDto(ClusteringRun run) {
        if (run == null) {
            return null;
        }

        List<ClusteringDtos.AssignmentDto> assignments = run.getAssignments() == null
                ? null
                : run.getAssignments().stream().map(this::toAssignmentDto).toList();
        Map<String, Object> metadata = run.getMetadata() == null
                ? null
                : new LinkedHashMap<>(run.getMetadata());

        return new ClusteringDtos.ClusteringRunDto(
                run.getId(),
                run.getName(),
                run.getKClusters(),
                run.getYear(),
                run.getAlgorithm(),
                run.getIterations(),
                run.getInertia(),
                metadata,
                run.getCreatedAt(),
                assignments
        );
    }

    // TrendModel
    default TrendDtos.TrendModelDto toTrendModelDto(TrendModel model) {
        if (model == null) {
            return null;
        }

        Long regionId = model.getRegion() != null ? model.getRegion().getId() : null;
        String regionName = model.getRegion() != null ? model.getRegion().getName() : null;
        Long indicatorId = model.getIndicator() != null ? model.getIndicator().getId() : null;
        String indicatorName = model.getIndicator() != null ? model.getIndicator().getName() : null;
        Map<String, Object> parameters = model.getParameters() == null
                ? null
                : new LinkedHashMap<>(model.getParameters());

        return new TrendDtos.TrendModelDto(
                model.getId(),
                regionId,
                regionName,
                indicatorId,
                indicatorName,
                model.getModelType(),
                model.getSlope(),
                model.getIntercept(),
                model.getRSquared(),
                model.getForecastYear(),
                model.getForecastValue(),
                parameters,
                model.getCreatedAt()
        );
    }
}
