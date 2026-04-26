package com.platform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class KMeansAlgorithmTest {

    private KMeansAlgorithm kMeans;

    @BeforeEach
    void setUp() { kMeans = new KMeansAlgorithm(); }

    @Test
    void cluster_shouldAssignAllPointsToLabels() {
        double[][] data = {{1, 1}, {1.1, 1.1}, {5, 5}, {5.1, 5.1}, {9, 9}, {9.1, 9.1}};
        KMeansAlgorithm.KMeansResult result = kMeans.cluster(data, 3, 42L);

        assertThat(result.labels()).hasSize(6);
        assertThat(result.centroids().length).isEqualTo(3);
        assertThat(result.iterations()).isGreaterThan(0);
        assertThat(result.inertia()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void cluster_shouldGroupClosePointsTogether() {
        double[][] data = {{0, 0}, {0.1, 0.1}, {10, 10}, {10.1, 10.1}};
        KMeansAlgorithm.KMeansResult result = kMeans.cluster(data, 2, 42L);

        // Points 0,1 should share a label; points 2,3 should share a label
        assertThat(result.labels()[0]).isEqualTo(result.labels()[1]);
        assertThat(result.labels()[2]).isEqualTo(result.labels()[3]);
        assertThat(result.labels()[0]).isNotEqualTo(result.labels()[2]);
    }

    @Test
    void cluster_withK1_shouldAssignAllToSameCluster() {
        double[][] data = {{1, 2}, {3, 4}, {5, 6}};
        KMeansAlgorithm.KMeansResult result = kMeans.cluster(data, 1, 0L);

        assertThat(result.labels()).containsOnly(0);
        assertThat(result.centroids().length).isEqualTo(1);
    }

    @Test
    void cluster_inertia_shouldBePositive() {
        double[][] data = {{1, 2}, {3, 4}, {5, 6}, {7, 8}};
        KMeansAlgorithm.KMeansResult result = kMeans.cluster(data, 2, 1L);
        assertThat(result.inertia()).isGreaterThan(0);
    }
}
