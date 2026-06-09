package com.ensah.nlp_annotation_platform.service.dataset;

import com.ensah.nlp_annotation_platform.dto.response.dataset.DatasetResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DatasetService {
    void uploadDataset(MultipartFile file, String tags);
    List<DatasetResponse> listDatasets();
    Object getDatasetDetail(Long id);
    void assignAnnotators(Long id, List<Long> annotatorIds);
    void removeAnnotator(Long id, Long userId);
}
