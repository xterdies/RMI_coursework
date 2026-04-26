package com.platform.service.export;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ClusteringRunExporterFactory {

    private final Map<ExportFormat, ClusteringRunExporter> exporters;

    public ClusteringRunExporterFactory(List<ClusteringRunExporter> exporters) {
        EnumMap<ExportFormat, ClusteringRunExporter> map = new EnumMap<>(ExportFormat.class);
        for (ClusteringRunExporter exporter : exporters) {
            map.put(exporter.format(), exporter);
        }
        this.exporters = Map.copyOf(map);
    }

    // Design Pattern: Factory - selects exporter implementation by format.
    public ClusteringRunExporter get(ExportFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Export format must not be null");
        }
        ClusteringRunExporter exporter = exporters.get(format);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
        return exporter;
    }
}

