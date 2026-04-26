package com.platform.service;

import com.platform.api.dto.RegionDtos;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.Region;
import com.platform.domain.repository.RegionRepository;
import com.platform.service.exception.ConflictException;
import com.platform.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    @Mock RegionRepository regionRepository;
    @Mock EntityMapper mapper;
    @InjectMocks RegionService regionService;

    @Test
    void findAll_shouldReturnPagedResults() {
        Region region = buildRegion(1L, "UA-KY");
        RegionDtos.RegionDto dto = buildDto(1L, "UA-KY");
        when(regionRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(region)));
        when(mapper.toRegionDto(region)).thenReturn(dto);

        var result = regionService.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("UA-KY");
    }

    @Test
    void findById_whenExists_shouldReturnDto() {
        Region region = buildRegion(1L, "UA-KY");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(mapper.toRegionDto(region)).thenReturn(buildDto(1L, "UA-KY"));

        var result = regionService.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_whenNotExists_shouldThrow() {
        when(regionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> regionService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_whenCodeExists_shouldThrowConflict() {
        when(regionRepository.existsByCode("UA-KY")).thenReturn(true);
        var request = new RegionDtos.CreateRegionRequest("UA-KY", "Kyiv", "UA", null, null, null, null);
        assertThatThrownBy(() -> regionService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_whenCodeNew_shouldSaveAndReturn() {
        Region region = buildRegion(1L, "UA-KY");
        RegionDtos.RegionDto dto = buildDto(1L, "UA-KY");
        var request = new RegionDtos.CreateRegionRequest("UA-KY", "Kyiv", "UA", null, null, null, null);

        when(regionRepository.existsByCode("UA-KY")).thenReturn(false);
        when(mapper.toRegion(request)).thenReturn(region);
        when(regionRepository.save(region)).thenReturn(region);
        when(mapper.toRegionDto(region)).thenReturn(dto);

        var result = regionService.create(request);
        assertThat(result.code()).isEqualTo("UA-KY");
        verify(regionRepository).save(region);
    }

    @Test
    void delete_whenNotExists_shouldThrow() {
        when(regionRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> regionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenExists_shouldCallDeleteById() {
        when(regionRepository.existsById(1L)).thenReturn(true);
        regionService.delete(1L);
        verify(regionRepository).deleteById(1L);
    }

    private Region buildRegion(Long id, String code) {
        Region r = new Region();
        r.setId(id);
        r.setCode(code);
        r.setName("Test Region");
        r.setCountryCode("UA");
        return r;
    }

    private RegionDtos.RegionDto buildDto(Long id, String code) {
        return new RegionDtos.RegionDto(id, code, "Test Region", "UA", null, null, null, null, null);
    }
}
