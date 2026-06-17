package com.ensah.nlp_annotation_platform.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalDatasets;
    private long totalTexts;
    private long totalAnnotators;
    private long totalAnnotations;
    private double overallAnnotationPercent;
    private Map<String, Long> globalClassDistribution;
    private List<AnnotatorProgressEntry> annotatorProgress;
    private List<Long> spammerIds;
}
