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
    private Double progress;
    private List<AnnotatorInfo> annotators;
    private List<TextItemInfo> textItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnotatorInfo {
        private Long id;
        private String username;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextItemInfo {
        private Long id;
        private String sourceText;
        private String targetText;
    }
}
