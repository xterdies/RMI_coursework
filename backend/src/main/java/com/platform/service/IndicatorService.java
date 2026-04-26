package com.platform.service;

import com.platform.api.dto.IndicatorDtos;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.EconomicIndicator;
import com.platform.domain.entity.IndicatorValue;
import com.platform.domain.entity.Region;
import com.platform.domain.repository.EconomicIndicatorRepository;
import com.platform.domain.repository.IndicatorValueRepository;
import com.platform.service.exception.ConflictException;
import com.platform.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicatorService {

    private final EconomicIndicatorRepository indicatorRepository;
    private final IndicatorValueRepository valueRepository;
    private final RegionService regionService;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<IndicatorDtos.IndicatorDto> findAllIndicators() {
        return indicatorRepository.findAll().stream().map(mapper::toIndicatorDto).toList();
    }

    @Transactional(readOnly = true)
    public IndicatorDtos.IndicatorDto findIndicatorById(Long id) {
        return mapper.toIndicatorDto(getIndicatorOrThrow(id));
    }

    @Transactional
    public IndicatorDtos.IndicatorDto createIndicator(IndicatorDtos.CreateIndicatorRequest request) {
        if (indicatorRepository.findByCode(request.code()).isPresent()) {
            throw new ConflictException("Indicator code already exists: " + request.code());
        }
        return mapper.toIndicatorDto(indicatorRepository.save(mapper.toIndicator(request)));
    }

    @Transactional
    public void deleteIndicator(Long id) {
        if (!indicatorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Indicator not found: " + id);
        }
        indicatorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<IndicatorDtos.IndicatorValueDto> findValues(Long regionId, Long indicatorId, Pageable pageable) {
        return valueRepository.findAll(pageable).map(mapper::toIndicatorValueDto);
    }

    @Transactional(readOnly = true)
    public List<IndicatorDtos.IndicatorValueDto> findValuesByRegionAndIndicator(Long regionId, Long indicatorId) {
        return valueRepository.findByRegionIdAndIndicatorIdOrderByYear(regionId, indicatorId)
                .stream().map(mapper::toIndicatorValueDto).toList();
    }

    @Transactional
    public IndicatorDtos.IndicatorValueDto createValue(IndicatorDtos.CreateIndicatorValueRequest request) {
        Region region = regionService.getOrThrow(request.regionId());
        EconomicIndicator indicator = getIndicatorOrThrow(request.indicatorId());

        valueRepository.findByRegionIdAndIndicatorIdAndYear(request.regionId(), request.indicatorId(), request.year())
                .ifPresent(v -> { throw new ConflictException("Value already exists for this region/indicator/year"); });

        IndicatorValue value = IndicatorValue.builder()
                .region(region)
                .indicator(indicator)
                .year(request.year())
                .value(request.value())
                .sourceUrl(request.sourceUrl())
                .build();
        return mapper.toIndicatorValueDto(valueRepository.save(value));
    }

    @Transactional
    public void deleteValue(Long id) {
        if (!valueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Indicator value not found: " + id);
        }
        valueRepository.deleteById(id);
    }

    public EconomicIndicator getIndicatorOrThrow(Long id) {
        return indicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found: " + id));
    }
}
