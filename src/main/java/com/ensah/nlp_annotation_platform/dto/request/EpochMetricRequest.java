package com.ensah.nlp_annotation_platform.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpochMetricRequest {
    private Long jobId;
    private Integer epoch;
    private Double loss;
    private Double accuracy;
    private Double evalLoss;
    private Double evalAccuracy;
}
