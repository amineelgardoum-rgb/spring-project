package com.ensah.nlp_annotation_platform.service.nlp;

import com.ensah.nlp_annotation_platform.dto.response.NlpLogResponse;

import java.util.List;
import java.util.Map;

public interface NlpService {
    Long startTraining(Map<String, Object> hyperparameters, String username);
    Long startTesting(String username);
    List<NlpLogResponse> getTrainingLogs();
}
