package com.ensah.nlp_annotation_platform.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotatorProgressEntry {
    private Long annotatorId;
    private String firstName;
    private String lastName;
    private long annotatedCount;
    private double avgTimePerAnnotation;
    private Map<String, Long> classDistribution;
}
