package com.ensah.nlp_annotation_platform.dto.request.dataset;

import lombok.Data;
import java.util.List;

@Data
public class DatasetAssignmentRequest {
    private List<Long> annotatorIds;
}
