package com.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure K-Means implementation operating on double[][] feature matrix.
 * Rows = data points, Columns = features.
 */
@Component
@Slf4j
public class KMeansAlgorithm {

    private static final int MAX_ITERATIONS = 300;
    private static final double CONVERGENCE_THRESHOLD = 1e-6;

    public record KMeansResult(int[] labels, double[][] centroids, int iterations, double inertia) {}

    public KMeansResult cluster(double[][] data, int k, long seed) {
        int n = data.length;
        int dims = data[0].length;
        double[][] centroids = initializeCentroids(data, k, seed);
        int[] labels = new int[n];
        int iter = 0;

        for (; iter < MAX_ITERATIONS; iter++) {
            int[] newLabels = assignLabels(data, centroids);
            double[][] newCentroids = recomputeCentroids(data, newLabels, k, dims);

            if (hasConverged(centroids, newCentroids)) {
                labels = newLabels;
                centroids = newCentroids;
                iter++;
                break;
            }
            labels = newLabels;
            centroids = newCentroids;
        }

        double inertia = computeInertia(data, labels, centroids);
        log.debug("K-Means converged in {} iterations, inertia={}", iter, inertia);
        return new KMeansResult(labels, centroids, iter, inertia);
    }

    private double[][] initializeCentroids(double[][] data, int k, long seed) {
        Random rng = new Random(seed);
        List<Integer> indices = new ArrayList<>();
        while (indices.size() < k) {
            int idx = rng.nextInt(data.length);
            if (!indices.contains(idx)) indices.add(idx);
        }
        double[][] centroids = new double[k][data[0].length];
        for (int i = 0; i < k; i++) {
            centroids[i] = data[indices.get(i)].clone();
        }
        return centroids;
    }

    private int[] assignLabels(double[][] data, double[][] centroids) {
        int[] labels = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            double minDist = Double.MAX_VALUE;
            for (int c = 0; c < centroids.length; c++) {
                double dist = euclideanSquared(data[i], centroids[c]);
                if (dist < minDist) {
                    minDist = dist;
                    labels[i] = c;
                }
            }
        }
        return labels;
    }

    private double[][] recomputeCentroids(double[][] data, int[] labels, int k, int dims) {
        double[][] sums = new double[k][dims];
        int[] counts = new int[k];
        for (int i = 0; i < data.length; i++) {
            int c = labels[i];
            counts[c]++;
            for (int d = 0; d < dims; d++) sums[c][d] += data[i][d];
        }
        double[][] centroids = new double[k][dims];
        for (int c = 0; c < k; c++) {
            if (counts[c] > 0) {
                for (int d = 0; d < dims; d++) centroids[c][d] = sums[c][d] / counts[c];
            }
        }
        return centroids;
    }

    private boolean hasConverged(double[][] old, double[][] updated) {
        for (int c = 0; c < old.length; c++) {
            if (euclideanSquared(old[c], updated[c]) > CONVERGENCE_THRESHOLD) return false;
        }
        return true;
    }

    private double computeInertia(double[][] data, int[] labels, double[][] centroids) {
        double total = 0;
        for (int i = 0; i < data.length; i++) {
            total += euclideanSquared(data[i], centroids[labels[i]]);
        }
        return total;
    }

    private double euclideanSquared(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }
}
