package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByDataset_Id(Long datasetId);
    List<Assignment> findByAnnotator_Id(Long annotatorId);
    Optional<Assignment> findByDataset_IdAndAnnotator_Id(Long datasetId, Long annotatorId);
}
