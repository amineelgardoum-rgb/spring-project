package com.ensah.nlp_annotation_platform.service.admin;

import com.ensah.nlp_annotation_platform.dto.request.admin.UpdateAnnotationRequest;
import com.ensah.nlp_annotation_platform.dto.response.admin.AnnotationAdminResponse;

import java.util.List;

public interface AdminAnnotationService {
    List<AnnotationAdminResponse> getAnnotationsByTextItem(Long textItemId);
    AnnotationAdminResponse updateAnnotation(Long id, UpdateAnnotationRequest request);
}
