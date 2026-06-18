package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.dto.request.annotator.AnnotateRequest;
import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorStatsResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorTaskResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.TextPairResponse;
import com.ensah.nlp_annotation_platform.service.annotator.AnnotatorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/annotator")
public class AnnotatorController {

    private final AnnotatorService annotatorService;

    public AnnotatorController(AnnotatorService annotatorService) {
        this.annotatorService = annotatorService;
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ANNOTATOR')")
    public ResponseEntity<Page<AnnotatorTaskResponse>> getTasks(
            @AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        return ResponseEntity.ok(annotatorService.getTasks(userDetails.getUsername(), pageable));
    }

    @GetMapping("/tasks/{taskId}/pairs")
    @PreAuthorize("hasRole('ANNOTATOR')")
    public ResponseEntity<Page<TextPairResponse>> getTextPairs(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(annotatorService.getTextPairs(taskId, userDetails.getUsername(), pageable));
    }

    @PostMapping("/tasks/{taskId}/annotate")
    @PreAuthorize("hasRole('ANNOTATOR')")
    public ResponseEntity<Map<String, String>> submitAnnotation(
            @PathVariable Long taskId,
            @Valid @RequestBody AnnotateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        annotatorService.submitAnnotation(taskId, request.getTextItemId(), request.getLabel(), userDetails.getUsername());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ANNOTATOR')")
    public ResponseEntity<AnnotatorStatsResponse> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(annotatorService.getStats(userDetails.getUsername()));
    }
}
