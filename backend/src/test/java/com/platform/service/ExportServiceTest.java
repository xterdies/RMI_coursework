package com.platform.service;

import com.platform.service.export.ClusteringRunExporter;
import com.platform.service.export.ClusteringRunExporterFactory;
import com.platform.service.export.ExportFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock ClusteringRunExporterFactory factory;
    @Mock ClusteringRunExporter exporter;
    @InjectMocks ExportService exportService;

    @Test
    void exportPdf_delegatesToFactoryExporter() {
        when(factory.get(ExportFormat.PDF)).thenReturn(exporter);
        when(exporter.export(10L)).thenReturn(new byte[]{1, 2, 3});

        byte[] bytes = exportService.exportClusteringToPdf(10L);

        assertThat(bytes).containsExactly(1, 2, 3);
        verify(factory).get(ExportFormat.PDF);
        verify(exporter).export(10L);
    }

    @Test
    void exportExcel_delegatesToFactoryExporter() {
        when(factory.get(ExportFormat.EXCEL)).thenReturn(exporter);
        when(exporter.export(11L)).thenReturn(new byte[]{9});

        byte[] bytes = exportService.exportClusteringToExcel(11L);

        assertThat(bytes).containsExactly(9);
        verify(factory).get(ExportFormat.EXCEL);
        verify(exporter).export(11L);
    }
}

