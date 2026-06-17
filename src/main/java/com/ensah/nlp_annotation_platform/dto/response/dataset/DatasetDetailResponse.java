package com.ensah.nlp_annotation_platform.dto.response.dataset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String filePath;
    private Integer numRecords;
    private String createdBy;
    private List<String> labels;
    private List<Long> assignedAnnotatorIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
