package com.ensah.nlp_annotation_platform.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationAdminResponse {
    private Long id;
    private Long annotatorId;
    private String annotatorName;
    private String label;
    private String comment;
    private Instant createdAt;
}
