package com.ensah.nlp_annotation_platform.service.annotator;

import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorStatsResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorTaskResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.TextPairResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnnotatorService {
    Page<AnnotatorTaskResponse> getTasks(String username, Pageable pageable);
    Page<TextPairResponse> getTextPairs(Long assignmentId, String username, Pageable pageable);
    void submitAnnotation(Long textItemId, String label, Double duration, String username);
    AnnotatorStatsResponse getStats(String username);
}
