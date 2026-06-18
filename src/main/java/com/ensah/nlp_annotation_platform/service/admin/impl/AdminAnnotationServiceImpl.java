package com.ensah.nlp_annotation_platform.service.admin.impl;

import com.ensah.nlp_annotation_platform.domain.Annotation;
import com.ensah.nlp_annotation_platform.dto.request.admin.UpdateAnnotationRequest;
import com.ensah.nlp_annotation_platform.dto.response.admin.AnnotationAdminResponse;
import com.ensah.nlp_annotation_platform.exception.ResourceNotFoundException;
import com.ensah.nlp_annotation_platform.exception.ValidationException;
import com.ensah.nlp_annotation_platform.repository.AnnotationRepository;
import com.ensah.nlp_annotation_platform.service.admin.AdminAnnotationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminAnnotationServiceImpl implements AdminAnnotationService {

    private final AnnotationRepository annotationRepository;

    public AdminAnnotationServiceImpl(AnnotationRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnotationAdminResponse> getAnnotationsByTextItem(Long textItemId) {
        return annotationRepository.findByTextItem_Id(textItemId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AnnotationAdminResponse updateAnnotation(Long id, UpdateAnnotationRequest request) {
        Annotation annotation = annotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annotation not found with id " + id));

        if (!annotation.getTextItem().getDataset().getLabels().contains(request.getLabel())) {
            throw new ValidationException("Label '" + request.getLabel() + "' is not valid for this dataset");
        }

        annotation.setLabel(request.getLabel());
        annotation.setComment(request.getComment());
        annotationRepository.save(annotation);
        return toResponse(annotation);
    }

    private AnnotationAdminResponse toResponse(Annotation annotation) {
        return AnnotationAdminResponse.builder()
                .id(annotation.getId())
                .annotatorId(annotation.getAnnotator().getId())
                .annotatorName(annotation.getAnnotator().getUsername())
                .label(annotation.getLabel())
                .comment(annotation.getComment())
                .createdAt(annotation.getCreatedAt())
                .build();
    }
}
