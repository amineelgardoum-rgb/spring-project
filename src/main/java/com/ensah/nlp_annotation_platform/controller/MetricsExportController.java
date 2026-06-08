package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.service.metrics.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/admin")
public class MetricsExportController {

    private final MetricsService metricsService;

    public MetricsExportController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/datasets/{id}/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMetrics(@PathVariable Long id) {
        return ResponseEntity.ok(metricsService.computeMetrics(id));
    }

    @GetMapping("/datasets/{id}/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> exportDataset(@PathVariable Long id, @RequestParam(defaultValue = "csv") String format) {
        return metricsService.exportDataset(id, format);
    }
}
