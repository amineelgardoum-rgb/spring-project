package com.ensah.nlp_annotation_platform.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAnnotationRequest {
    @NotBlank(message = "label is required")
    private String label;

    private String comment;
}
