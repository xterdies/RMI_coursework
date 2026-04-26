package com.platform.api.mapper;

import com.platform.api.dto.*;
import com.platform.domain.entity.*;
import org.mapstruct.*;

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
    @Mapping(target = "assignments", source = "assignments")
    ClusteringDtos.ClusteringRunDto toClusteringRunDto(ClusteringRun run);

    // TrendModel
    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "indicatorId", source = "indicator.id")
    @Mapping(target = "indicatorName", source = "indicator.name")
    TrendDtos.TrendModelDto toTrendModelDto(TrendModel model);
}
