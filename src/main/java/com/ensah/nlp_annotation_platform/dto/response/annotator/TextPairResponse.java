package com.ensah.nlp_annotation_platform.dto.response.annotator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextPairResponse {
    private Long textItemId;
    private String content;
    private String pairContent;
    private String metadata;
    private List<String> availableLabels;
    private String currentLabel;
    private String currentComment;
}
