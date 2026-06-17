package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.dto.request.admin.UpdateAnnotationRequest;
import com.ensah.nlp_annotation_platform.dto.response.admin.AnnotationAdminResponse;
import com.ensah.nlp_annotation_platform.service.admin.AdminAnnotationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/annotations")
public class AdminAnnotationController {

    private final AdminAnnotationService adminAnnotationService;

    public AdminAnnotationController(AdminAnnotationService adminAnnotationService) {
        this.adminAnnotationService = adminAnnotationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnnotationAdminResponse>> getAnnotations(@RequestParam Long textItemId) {
        return ResponseEntity.ok(adminAnnotationService.getAnnotationsByTextItem(textItemId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnotationAdminResponse> updateAnnotation(@PathVariable Long id,
                                                                     @Valid @RequestBody UpdateAnnotationRequest request) {
        return ResponseEntity.ok(adminAnnotationService.updateAnnotation(id, request));
    }
}
