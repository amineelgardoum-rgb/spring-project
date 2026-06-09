package com.ensah.nlp_annotation_platform.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class NlpTrainRequest {
    private Map<String, Object> hyperparameters;
}
