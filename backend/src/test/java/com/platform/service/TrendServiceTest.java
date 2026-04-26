package com.platform.service;

import com.platform.api.dto.TrendDtos;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.*;
import com.platform.domain.repository.IndicatorValueRepository;
import com.platform.domain.repository.TrendModelRepository;
import com.platform.service.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendServiceTest {

    @Mock TrendModelRepository trendModelRepository;
    @Mock IndicatorValueRepository indicatorValueRepository;
    @Mock RegionService regionService;
    @Mock IndicatorService indicatorService;
    @Mock EntityMapper mapper;
    @InjectMocks TrendService trendService;

    @Test
    void computeTrend_withInsufficientData_shouldThrow() {
        Region region = new Region(); region.setId(1L);
        EconomicIndicator indicator = new EconomicIndicator(); indicator.setId(1L);

        when(regionService.getOrThrow(1L)).thenReturn(region);
        when(indicatorService.getIndicatorOrThrow(1L)).thenReturn(indicator);
        when(indicatorValueRepository.findByRegionIdAndIndicatorIdOrderByYear(1L, 1L))
                .thenReturn(List.of(buildValue(2020, 100.0)));

        var request = new TrendDtos.TrendRequest(1L, 1L, 2025);
        assertThatThrownBy(() -> trendService.computeTrend(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least 2 data points");
    }

    @Test
    void computeTrend_withValidData_shouldReturnModel() {
        Region region = new Region(); region.setId(1L); region.setName("Test");
        EconomicIndicator indicator = new EconomicIndicator(); indicator.setId(1L); indicator.setName("GDP");

        List<IndicatorValue> values = List.of(
                buildValue(2018, 100.0), buildValue(2019, 110.0),
                buildValue(2020, 120.0), buildValue(2021, 130.0));

        TrendModel savedModel = new TrendModel();
        savedModel.setId(1L);
        savedModel.setRegion(region);
        savedModel.setIndicator(indicator);
        savedModel.setForecastYear(2025);
        savedModel.setForecastValue(BigDecimal.valueOf(170.0));

        when(regionService.getOrThrow(1L)).thenReturn(region);
        when(indicatorService.getIndicatorOrThrow(1L)).thenReturn(indicator);
        when(indicatorValueRepository.findByRegionIdAndIndicatorIdOrderByYear(1L, 1L)).thenReturn(values);
        when(trendModelRepository.findByRegionIdAndIndicatorIdAndForecastYear(1L, 1L, 2025))
                .thenReturn(Optional.empty());
        when(trendModelRepository.save(any())).thenReturn(savedModel);
        when(mapper.toTrendModelDto(savedModel)).thenReturn(
                new TrendDtos.TrendModelDto(1L, 1L, "Test", 1L, "GDP", "LINEAR",
                        BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ONE, 2025,
                        BigDecimal.valueOf(170), null, null));

        var result = trendService.computeTrend(new TrendDtos.TrendRequest(1L, 1L, 2025));
        assertThat(result.forecastYear()).isEqualTo(2025);
        verify(trendModelRepository).save(any(TrendModel.class));
    }

    private IndicatorValue buildValue(int year, double val) {
        IndicatorValue iv = new IndicatorValue();
        iv.setYear(year);
        iv.setValue(BigDecimal.valueOf(val));
        return iv;
    }
}
