package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
}
