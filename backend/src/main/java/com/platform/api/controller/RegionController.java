package com.platform.api.controller;

import com.platform.api.dto.CommonDtos;
import com.platform.api.dto.RegionDtos;
import com.platform.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
@Tag(name = "Regions", description = "Region management")
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    @Operation(summary = "List all regions (paginated)")
    public CommonDtos.PagedResponse<RegionDtos.RegionDto> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return CommonDtos.PagedResponse.from(regionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get region by ID")
    public RegionDtos.RegionDto getById(@PathVariable Long id) {
        return regionService.findById(id);
    }

    @GetMapping("/country/{countryCode}")
    @Operation(summary = "Get regions by country code")
    public List<RegionDtos.RegionDto> getByCountry(@PathVariable String countryCode) {
        return regionService.findByCountry(countryCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new region")
    public RegionDtos.RegionDto create(@Valid @RequestBody RegionDtos.CreateRegionRequest request) {
        return regionService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update region")
    public RegionDtos.RegionDto update(@PathVariable Long id,
                                        @RequestBody RegionDtos.UpdateRegionRequest request) {
        return regionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete region")
    public void delete(@PathVariable Long id) {
        regionService.delete(id);
    }
}
