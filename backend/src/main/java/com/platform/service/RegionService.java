package com.platform.service;

import com.platform.api.dto.RegionDtos;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.Region;
import com.platform.domain.repository.RegionRepository;
import com.platform.service.exception.ConflictException;
import com.platform.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public Page<RegionDtos.RegionDto> findAll(Pageable pageable) {
        return regionRepository.findAll(pageable).map(mapper::toRegionDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "regions", key = "#id")
    public RegionDtos.RegionDto findById(Long id) {
        return mapper.toRegionDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<RegionDtos.RegionDto> findByCountry(String countryCode) {
        return regionRepository.findByCountryCode(countryCode).stream()
                .map(mapper::toRegionDto).toList();
    }

    @Transactional
    @CacheEvict(value = "regions", allEntries = true)
    public RegionDtos.RegionDto create(RegionDtos.CreateRegionRequest request) {
        if (regionRepository.existsByCode(request.code())) {
            throw new ConflictException("Region code already exists: " + request.code());
        }
        return mapper.toRegionDto(regionRepository.save(mapper.toRegion(request)));
    }

    @Transactional
    @CacheEvict(value = "regions", key = "#id")
    public RegionDtos.RegionDto update(Long id, RegionDtos.UpdateRegionRequest request) {
        Region region = getOrThrow(id);
        if (request.name() != null) region.setName(request.name());
        if (request.countryCode() != null) region.setCountryCode(request.countryCode());
        if (request.latitude() != null) region.setLatitude(request.latitude());
        if (request.longitude() != null) region.setLongitude(request.longitude());
        if (request.population() != null) region.setPopulation(request.population());
        if (request.areaKm2() != null) region.setAreaKm2(request.areaKm2());
        return mapper.toRegionDto(regionRepository.save(region));
    }

    @Transactional
    @CacheEvict(value = "regions", key = "#id")
    public void delete(Long id) {
        if (!regionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Region not found: " + id);
        }
        regionRepository.deleteById(id);
    }

    public Region getOrThrow(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found: " + id));
    }
}
