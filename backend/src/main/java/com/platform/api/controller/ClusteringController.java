package com.platform.api.controller;

import com.platform.api.dto.ClusteringDtos;
import com.platform.api.dto.CommonDtos;
import com.platform.domain.entity.User;
import com.platform.service.ClusteringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clustering")
@RequiredArgsConstructor
@Tag(name = "Clustering", description = "K-Means clustering operations")
public class ClusteringController {

    private final ClusteringService clusteringService;

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Execute K-Means clustering")
    public ClusteringDtos.ClusteringRunDto run(@Valid @RequestBody ClusteringDtos.ClusteringRequest request,
                                                @AuthenticationPrincipal User user) {
        return clusteringService.runClustering(request, user);
    }

    @GetMapping
    @Operation(summary = "List all clustering runs")
    public CommonDtos.PagedResponse<ClusteringDtos.ClusteringRunDto> list(
            @PageableDefault(size = 10) Pageable pageable) {
        return CommonDtos.PagedResponse.from(clusteringService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get clustering run with assignments")
    public ClusteringDtos.ClusteringRunDto getById(@PathVariable Long id) {
        return clusteringService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete clustering run")
    public void delete(@PathVariable Long id) {
        clusteringService.delete(id);
    }
}
