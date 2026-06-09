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
public class JobResponse {
    private Long id;
    private String type;
    private String status;
    private Integer progress;
    private Long triggeredById;
    private String triggeredByUsername;
    private String hyperparameters;
    private String result;
    private String executionLogs;
    private String errorMessage;
    private Instant createdAt;
    private Instant completedAt;
}
