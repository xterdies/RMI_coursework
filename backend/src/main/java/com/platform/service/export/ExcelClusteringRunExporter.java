package com.platform.service.export;

import com.platform.api.dto.ClusteringDtos;
import com.platform.service.ClusteringService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ExcelClusteringRunExporter implements ClusteringRunExporter {

    private final ClusteringService clusteringService;

    @Override
    public ExportFormat format() {
        return ExportFormat.EXCEL;
    }

    @Override
    public byte[] export(Long runId) {
        ClusteringDtos.ClusteringRunDto run = clusteringService.findById(runId);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clustering Results");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Region");
            header.createCell(1).setCellValue("Code");
            header.createCell(2).setCellValue("Cluster");
            header.createCell(3).setCellValue("Distance to Centroid");

            int rowIdx = 1;
            for (ClusteringDtos.AssignmentDto a : run.assignments()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.regionName());
                row.createCell(1).setCellValue(a.regionCode());
                row.createCell(2).setCellValue(a.clusterLabel());
                if (a.distanceToCentroid() != null) {
                    row.createCell(3).setCellValue(a.distanceToCentroid().doubleValue());
                }
            }

            Sheet meta = workbook.createSheet("Metadata");
            meta.createRow(0).createCell(0).setCellValue("Run Name");
            meta.getRow(0).createCell(1).setCellValue(run.name());
            meta.createRow(1).createCell(0).setCellValue("Year");
            meta.getRow(1).createCell(1).setCellValue(run.year());
            meta.createRow(2).createCell(0).setCellValue("K Clusters");
            meta.getRow(2).createCell(1).setCellValue(run.kClusters());
            meta.createRow(3).createCell(0).setCellValue("Inertia");
            meta.getRow(3).createCell(1).setCellValue(run.inertia() != null ? run.inertia().doubleValue() : 0);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }
}

