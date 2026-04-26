package com.platform.service;

import com.platform.service.export.ClusteringRunExporterFactory;
import com.platform.service.export.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ClusteringRunExporterFactory exporterFactory;

    public byte[] exportClusteringToPdf(Long runId) {
        return exporterFactory.get(ExportFormat.PDF).export(runId);
    }

    public byte[] exportClusteringToExcel(Long runId) {
        return exporterFactory.get(ExportFormat.EXCEL).export(runId);
    }
}
