package com.platform.api.controller;

import com.platform.api.dto.IndicatorDtos;
import com.platform.service.IndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/indicators")
@RequiredArgsConstructor
@Tag(name = "Economic Indicators", description = "Indicator and value management")
public class IndicatorController {

    private final IndicatorService indicatorService;

    @GetMapping
    @Operation(summary = "List all indicators")
    public List<IndicatorDtos.IndicatorDto> listIndicators() {
        return indicatorService.findAllIndicators();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get indicator by ID")
    public IndicatorDtos.IndicatorDto getIndicator(@PathVariable Long id) {
        return indicatorService.findIndicatorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create indicator")
    public IndicatorDtos.IndicatorDto createIndicator(
            @Valid @RequestBody IndicatorDtos.CreateIndicatorRequest request) {
        return indicatorService.createIndicator(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete indicator")
    public void deleteIndicator(@PathVariable Long id) {
        indicatorService.deleteIndicator(id);
    }

    @GetMapping("/values/region/{regionId}/indicator/{indicatorId}")
    @Operation(summary = "Get time-series values for a region and indicator")
    public List<IndicatorDtos.IndicatorValueDto> getValues(@PathVariable Long regionId,
                                                            @PathVariable Long indicatorId) {
        return indicatorService.findValuesByRegionAndIndicator(regionId, indicatorId);
    }

    @PostMapping("/values")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add indicator value")
    public IndicatorDtos.IndicatorValueDto createValue(
            @Valid @RequestBody IndicatorDtos.CreateIndicatorValueRequest request) {
        return indicatorService.createValue(request);
    }

    @DeleteMapping("/values/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete indicator value")
    public void deleteValue(@PathVariable Long id) {
        indicatorService.deleteValue(id);
    }
}
