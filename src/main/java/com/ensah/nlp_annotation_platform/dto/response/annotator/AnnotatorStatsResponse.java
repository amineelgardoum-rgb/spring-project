package com.ensah.nlp_annotation_platform.dto.response.annotator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotatorStatsResponse {
    private long totalAnnotated;
    private double avgTimePerAnnotation;
    private Map<String, Long> classDistribution;
}
