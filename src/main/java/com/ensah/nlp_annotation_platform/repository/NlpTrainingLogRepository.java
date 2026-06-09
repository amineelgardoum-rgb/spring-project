package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.NlpTrainingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NlpTrainingLogRepository extends JpaRepository<NlpTrainingLog, Long> {
    List<NlpTrainingLog> findAllByOrderByStartedAtDesc();
}
