package com.platform.service;

import com.platform.api.dto.ClusteringDtos;
import com.platform.api.mapper.EntityMapper;
import com.platform.domain.entity.*;
import com.platform.domain.repository.*;
import com.platform.infrastructure.aop.MeasureExecutionTime;
import com.platform.infrastructure.query.FilterCriterion;
import com.platform.infrastructure.query.Specifications;
import com.platform.service.exception.ResourceNotFoundException;
import com.platform.service.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusteringService {

    private final ClusteringRunRepository clusteringRunRepository;
    private final IndicatorValueRepository indicatorValueRepository;
    private final RegionRepository regionRepository;
    private final KMeansAlgorithm kMeans;
    private final EntityMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(cacheNames = {"clusteringRuns:list","clusteringRuns:byId"}, allEntries = true)
    @MeasureExecutionTime(value = "clustering.run", logArgs = true)
    public ClusteringDtos.ClusteringRunDto runClustering(ClusteringDtos.ClusteringRequest request, User user) {
        List<Region> regions = regionRepository.findAll();
        if (regions.size() < request.kClusters()) {
            throw new ValidationException("Not enough regions (%d) for k=%d clusters"
                    .formatted(regions.size(), request.kClusters()));
        }

        // Build feature matrix: rows=regions, cols=indicators (z-score normalized)
        double[][] matrix = buildFeatureMatrix(regions, request.indicatorIds(), request.year());
        double[][] normalized = zScoreNormalize(matrix);

        KMeansAlgorithm.KMeansResult result = kMeans.cluster(normalized, request.kClusters(), 42L);

        ClusteringRun run = ClusteringRun.builder()
                .name(request.name())
                .kClusters(request.kClusters())
                .year(request.year())
                .algorithm("KMEANS")
                .iterations(result.iterations())
                .inertia(BigDecimal.valueOf(result.inertia()).setScale(6, RoundingMode.HALF_UP))
                .metadata(buildMetadata(request.indicatorIds(), result.centroids()))
                .createdBy(user)
                .build();

        for (int i = 0; i < regions.size(); i++) {
            double dist = Math.sqrt(euclideanSquared(normalized[i], result.centroids()[result.labels()[i]]));
            run.getAssignments().add(ClusteringAssignment.builder()
                    .run(run)
                    .region(regions.get(i))
                    .clusterLabel(result.labels()[i])
                    .distanceToCentroid(BigDecimal.valueOf(dist).setScale(6, RoundingMode.HALF_UP))
                    .build());
        }

        ClusteringRun saved = clusteringRunRepository.save(run);
        // Design Pattern: Observer/Event - publish domain event for side effects (audit/notifications) without coupling.
        eventPublisher.publishEvent(new com.platform.service.event.ClusteringRunCompletedEvent(saved.getId(), saved.getCreatedBy().getId()));
        return mapper.toClusteringRunDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ClusteringDtos.ClusteringRunDto> findAll(Pageable pageable) {
        return clusteringRunRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(mapper::toClusteringRunDto);
    }

    @Transactional(readOnly = true)
    public Page<ClusteringDtos.ClusteringRunDto> findAll(Pageable pageable, List<FilterCriterion> criteria) {
        Specification<ClusteringRun> spec = Specifications.fromCriteria(criteria, List.of("name", "algorithm", "year", "kClusters"));
        return clusteringRunRepository.findAll(spec, pageable).map(mapper::toClusteringRunDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "clusteringRuns:byId", key = "#id")
    public ClusteringDtos.ClusteringRunDto findById(Long id) {
        ClusteringRun run = clusteringRunRepository.findByIdWithAssignments(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clustering run not found: " + id));
        return mapper.toClusteringRunDto(run);
    }

    @Transactional
    @CacheEvict(cacheNames = {"clusteringRuns:list","clusteringRuns:byId"}, allEntries = true)
    public void delete(Long id) {
        if (!clusteringRunRepository.existsById(id)) {
            throw new ResourceNotFoundException("Clustering run not found: " + id);
        }
        clusteringRunRepository.deleteById(id);
    }

    private double[][] buildFeatureMatrix(List<Region> regions, List<Long> indicatorIds, Integer year) {
        double[][] matrix = new double[regions.size()][indicatorIds.size()];
        for (int r = 0; r < regions.size(); r++) {
            for (int c = 0; c < indicatorIds.size(); c++) {
                matrix[r][c] = indicatorValueRepository
                        .findByRegionIdAndIndicatorIdAndYear(regions.get(r).getId(), indicatorIds.get(c), year)
                        .map(v -> v.getValue().doubleValue())
                        .orElse(0.0);
            }
        }
        return matrix;
    }

    private double[][] zScoreNormalize(double[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        double[][] result = new double[rows][cols];
        for (int c = 0; c < cols; c++) {
            double mean = 0, std = 0;
            for (double[] row : matrix) mean += row[c];
            mean /= rows;
            for (double[] row : matrix) std += Math.pow(row[c] - mean, 2);
            std = Math.sqrt(std / rows);
            for (int r = 0; r < rows; r++) {
                result[r][c] = std == 0 ? 0 : (matrix[r][c] - mean) / std;
            }
        }
        return result;
    }

    private Map<String, Object> buildMetadata(List<Long> indicatorIds, double[][] centroids) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("indicatorIds", indicatorIds);
        meta.put("centroids", centroids);
        return meta;
    }

    private double euclideanSquared(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) { double d = a[i] - b[i]; sum += d * d; }
        return sum;
    }
}
