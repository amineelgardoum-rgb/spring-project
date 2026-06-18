package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.TextItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextItemRepository extends JpaRepository<TextItem, Long> {
    List<TextItem> findByDatasetId(Long datasetId);
    Page<TextItem> findByDatasetId(Long datasetId, Pageable pageable);
    long countByDatasetId(Long datasetId);
}
