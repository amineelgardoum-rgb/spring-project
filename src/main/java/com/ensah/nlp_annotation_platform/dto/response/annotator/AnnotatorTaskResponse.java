package com.ensah.nlp_annotation_platform.dto.response.annotator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotatorTaskResponse {
    private Long id;
    private Long datasetId;
    private String datasetName;
    private String datasetDescription;
    private long totalItems;
    private long annotatedItems;
    private double completionPercentage;
    private LocalDateTime assignedAt;
}
