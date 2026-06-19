package com.ensah.nlp_annotation_platform.service.metrics;

import org.springframework.http.ResponseEntity;

public interface MetricsService {
    Object computeMetrics(Long datasetId);
    ResponseEntity<byte[]> exportDataset(Long datasetId, String format);
}
