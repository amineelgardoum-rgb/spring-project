package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.TrainingMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingMetricRepository extends JpaRepository<TrainingMetric, Long> {
    List<TrainingMetric> findByJobIdOrderByEpochAsc(Long jobId);
}
