package com.ensah.nlp_annotation_platform.dto.response.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {
    private long totalItems;
    private long totalAnnotations;
    private long assignedAnnotators;
    private Map<String, Long> overallClassDistribution;
    private Double fleissKappa;
    private Map<Long, Long> annotationsPerAnnotator;
}
