package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.Annotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByTextItem_Dataset_Id(Long datasetId);
    Optional<Annotation> findByTextItem_IdAndAnnotator_Id(Long textItemId, Long annotatorId);
    long countByAnnotator_Id(Long annotatorId);
    long countByTextItem_Dataset_IdAndAnnotator_Id(Long datasetId, Long annotatorId);
    List<Annotation> findByAnnotator_Id(Long annotatorId);
    Page<Annotation> findByAnnotator_Id(Long annotatorId, Pageable pageable);
    List<Annotation> findByTextItem_Id(Long textItemId);
}
