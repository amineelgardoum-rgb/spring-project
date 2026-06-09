package com.ensah.nlp_annotation_platform.service.dataset.impl;

import com.ensah.nlp_annotation_platform.dto.response.dataset.DatasetResponse;
import com.ensah.nlp_annotation_platform.service.dataset.DatasetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

    @Override
    public void uploadDataset(MultipartFile file, String tags) {
        // Implementation for CSV/JSON parsing should go here
        // Should run asynchronously
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatasetResponse> listDatasets() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getDatasetDetail(Long id) {
        return null;
    }

    @Override
    public void assignAnnotators(Long id, List<Long> annotatorIds) {
    }

    @Override
    public void removeAnnotator(Long id, Long userId) {
    }
}
