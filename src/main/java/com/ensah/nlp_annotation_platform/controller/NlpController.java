package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.dto.request.NlpTrainRequest;
import com.ensah.nlp_annotation_platform.dto.response.NlpLogResponse;
import com.ensah.nlp_annotation_platform.service.nlp.NlpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/nlp")
public class NlpController {

    private final NlpService nlpService;

    public NlpController(NlpService nlpService) {
        this.nlpService = nlpService;
    }

    @PostMapping("/train")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> startTraining(@RequestBody NlpTrainRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long jobId = nlpService.startTraining(request.getHyperparameters(), userDetails.getUsername());
        return ResponseEntity.ok(Map.of("jobId", jobId));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> startTesting(@AuthenticationPrincipal UserDetails userDetails) {
        Long jobId = nlpService.startTesting(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("jobId", jobId));
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NlpLogResponse>> getTrainingLogs() {
        return ResponseEntity.ok(nlpService.getTrainingLogs());
    }
}
