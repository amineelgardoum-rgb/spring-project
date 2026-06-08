package com.ensah.nlp_annotation_platform.service.metrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface MetricsService {
    Object computeMetrics(Long datasetId);
    ResponseEntity<StreamingResponseBody> exportDataset(Long datasetId, String format);
}
