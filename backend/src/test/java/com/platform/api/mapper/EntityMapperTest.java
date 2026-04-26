package com.platform.api.mapper;

import com.platform.api.dto.ClusteringDtos;
import com.platform.api.dto.TrendDtos;
import com.platform.domain.entity.ClusteringRun;
import com.platform.domain.entity.EconomicIndicator;
import com.platform.domain.entity.Region;
import com.platform.domain.entity.TrendModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMapperTest {

    private final EntityMapper mapper = new EntityMapperImpl();

    @Test
    void toClusteringRunDto_shouldMapKClusters() {
        ClusteringRun run = ClusteringRun.builder()
                .id(7L)
                .name("Macroeconomic Snapshot")
                .kClusters(4)
                .year(2023)
                .algorithm("K_MEANS")
                .build();

        ClusteringDtos.ClusteringRunDto dto = mapper.toClusteringRunDto(run);

        assertThat(dto.kClusters()).isEqualTo(4);
    }

    @Test
    void toTrendModelDto_shouldMapRSquared() {
        Region region = Region.builder().id(3L).name("Bavaria").build();
        EconomicIndicator indicator = EconomicIndicator.builder().id(9L).name("GDP").build();
        TrendModel model = TrendModel.builder()
                .id(11L)
                .region(region)
                .indicator(indicator)
                .modelType("LINEAR")
                .slope(new BigDecimal("12.34567890"))
                .intercept(new BigDecimal("987.65432100"))
                .rSquared(new BigDecimal("0.991234"))
                .forecastYear(2026)
                .forecastValue(new BigDecimal("1500.2500"))
                .build();

        TrendDtos.TrendModelDto dto = mapper.toTrendModelDto(model);

        assertThat(dto.rSquared()).isEqualByComparingTo("0.991234");
    }
}
