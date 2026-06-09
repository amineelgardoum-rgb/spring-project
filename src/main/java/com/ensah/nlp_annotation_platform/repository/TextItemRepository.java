package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.TextItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextItemRepository extends JpaRepository<TextItem, Long> {
    List<TextItem> findByDatasetId(Long datasetId);
}
