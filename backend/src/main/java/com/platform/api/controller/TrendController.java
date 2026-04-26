package com.platform.api.controller;

import com.platform.api.dto.TrendDtos;
import com.platform.service.TrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trends")
@RequiredArgsConstructor
@Tag(name = "Trend Analysis", description = "Time-series trend forecasting")
public class TrendController {

    private final TrendService trendService;

    @PostMapping("/compute")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Compute linear trend and forecast for a region/indicator")
    public TrendDtos.TrendModelDto compute(@Valid @RequestBody TrendDtos.TrendRequest request) {
        return trendService.computeTrend(request);
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Get all trend models for a region")
    public List<TrendDtos.TrendModelDto> getByRegion(@PathVariable Long regionId) {
        return trendService.findByRegion(regionId);
    }
}
