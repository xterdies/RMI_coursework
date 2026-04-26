package com.platform.service.export;

public interface ClusteringRunExporter {
    ExportFormat format();
    byte[] export(Long runId);
}

