package com.platform.service.export;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.platform.api.dto.ClusteringDtos;
import com.platform.service.ClusteringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PdfClusteringRunExporter implements ClusteringRunExporter {

    private final ClusteringService clusteringService;

    @Override
    public ExportFormat format() {
        return ExportFormat.PDF;
    }

    @Override
    public byte[] export(Long runId) {
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
}

