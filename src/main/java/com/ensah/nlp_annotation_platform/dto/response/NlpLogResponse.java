package com.ensah.nlp_annotation_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpLogResponse {
    private Long id;
    private Instant startedAt;
    private Instant completedAt;
    private Long triggeredById;
    private String triggeredByUsername;
    private String hyperparameters;
    private String metrics;
    private Double accuracy;
    private Double f1Score;
    private Double loss;
    private String modelPath;
    private String status;
    private String executionLogs;
}
