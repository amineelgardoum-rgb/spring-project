package com.ensah.nlp_annotation_platform.dto.response.dataset;

import lombok.Data;

@Data
public class DatasetResponse {
    private Long id;
    private String name;
    private double completionPercentage;
}
