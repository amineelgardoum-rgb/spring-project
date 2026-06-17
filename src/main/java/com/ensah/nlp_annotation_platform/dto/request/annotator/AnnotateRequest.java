package com.ensah.nlp_annotation_platform.dto.request.annotator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnnotateRequest {
    @NotNull(message = "textItemId is required")
    private Long textItemId;

    @NotBlank(message = "label is required")
    private String label;
}
