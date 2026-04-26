package com.platform.service.export;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ClusteringRunExporterFactoryTest {

    @Test
    void factory_selectsExporterByFormat() {
        ClusteringRunExporter pdf = new ClusteringRunExporter() {
            @Override public ExportFormat format() { return ExportFormat.PDF; }
            @Override public byte[] export(Long runId) { return new byte[]{1}; }
        };
        ClusteringRunExporter excel = new ClusteringRunExporter() {
            @Override public ExportFormat format() { return ExportFormat.EXCEL; }
            @Override public byte[] export(Long runId) { return new byte[]{2}; }
        };

        ClusteringRunExporterFactory factory = new ClusteringRunExporterFactory(List.of(pdf, excel));

        assertThat(factory.get(ExportFormat.PDF)).isSameAs(pdf);
        assertThat(factory.get(ExportFormat.EXCEL)).isSameAs(excel);
        assertThatThrownBy(() -> factory.get(null)).isInstanceOf(IllegalArgumentException.class);
    }
}

