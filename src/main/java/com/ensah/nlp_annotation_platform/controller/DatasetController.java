package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.dto.request.dataset.DatasetAssignmentRequest;
import com.ensah.nlp_annotation_platform.dto.response.dataset.DatasetResponse;
import com.ensah.nlp_annotation_platform.service.dataset.DatasetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/datasets")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> uploadDataset(@RequestParam("file") MultipartFile file, @RequestParam("tags") String tags) {
        datasetService.uploadDataset(file, tags);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DatasetResponse>> listDatasets() {
        return ResponseEntity.ok(datasetService.listDatasets());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getDatasetDetail(@PathVariable Long id) {
        return ResponseEntity.ok(datasetService.getDatasetDetail(id));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignAnnotators(@PathVariable Long id, @RequestBody DatasetAssignmentRequest request) {
        datasetService.assignAnnotators(id, request.getAnnotatorIds());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/annotators/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeAnnotator(@PathVariable Long id, @PathVariable Long userId) {
        datasetService.removeAnnotator(id, userId);
        return ResponseEntity.noContent().build();
    }
}
