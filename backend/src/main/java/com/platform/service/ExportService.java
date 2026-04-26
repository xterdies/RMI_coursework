package com.platform.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.platform.api.dto.ClusteringDtos;
import com.platform.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ClusteringService clusteringService;

    public byte[] exportClusteringToPdf(Long runId) {
        ClusteringDtos.ClusteringRunDto run = clusteringService.findById(runId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.add(new Paragraph("Clustering Report: " + run.name())
                    .setBold().setFontSize(16));
            doc.add(new Paragraph("Year: %d | K: %d | Algorithm: %s | Inertia: %s"
                    .formatted(run.year(), run.kClusters(), run.algorithm(), run.inertia())));

            Table table = new Table(new float[]{3, 2, 2, 3});
            table.addHeaderCell(new Cell().add(new Paragraph("Region")));
            table.addHeaderCell(new Cell().add(new Paragraph("Code")));
            table.addHeaderCell(new Cell().add(new Paragraph("Cluster")));
            table.addHeaderCell(new Cell().add(new Paragraph("Distance")));

            for (ClusteringDtos.AssignmentDto a : run.assignments()) {
                table.addCell(a.regionName());
                table.addCell(a.regionCode());
                table.addCell(String.valueOf(a.clusterLabel()));
                table.addCell(a.distanceToCentroid() != null ? a.distanceToCentroid().toPlainString() : "-");
            }
            doc.add(table);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
        return baos.toByteArray();
    }

    public byte[] exportClusteringToExcel(Long runId) {
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
