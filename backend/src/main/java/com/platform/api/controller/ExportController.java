package com.platform.api.controller;

import com.platform.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "PDF and Excel export endpoints")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/clustering/{id}/pdf")
    @Operation(summary = "Export clustering run as PDF")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] pdf = exportService.exportClusteringToPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clustering-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/clustering/{id}/excel")
    @Operation(summary = "Export clustering run as Excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id) {
        byte[] excel = exportService.exportClusteringToExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clustering-" + id + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
