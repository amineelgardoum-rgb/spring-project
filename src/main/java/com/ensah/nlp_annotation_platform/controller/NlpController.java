package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.domain.NlpTrainingLog;
import com.ensah.nlp_annotation_platform.domain.TrainingMetric;
import com.ensah.nlp_annotation_platform.dto.request.EpochMetricRequest;
import com.ensah.nlp_annotation_platform.dto.response.NlpLogResponse;
import com.ensah.nlp_annotation_platform.repository.NlpTrainingLogRepository;
import com.ensah.nlp_annotation_platform.repository.TrainingMetricRepository;
import com.ensah.nlp_annotation_platform.service.nlp.MetricsSseService;
import com.ensah.nlp_annotation_platform.service.nlp.NlpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/nlp")
public class NlpController {

    private static final Logger log = LoggerFactory.getLogger(NlpController.class);

    private final NlpService nlpService;
    private final MetricsSseService metricsSseService;
    private final TrainingMetricRepository trainingMetricRepository;
    private final NlpTrainingLogRepository nlpTrainingLogRepository;

    @Value("${nlp.scripts.dir:python}")
    private String scriptsDir;

    @Value("${nlp.models.dir:${java.io.tmpdir}/nlp-models}")
    private String modelsDir;

    public NlpController(NlpService nlpService,
                         MetricsSseService metricsSseService,
                         TrainingMetricRepository trainingMetricRepository,
                         NlpTrainingLogRepository nlpTrainingLogRepository) {
        this.nlpService = nlpService;
        this.metricsSseService = metricsSseService;
        this.trainingMetricRepository = trainingMetricRepository;
        this.nlpTrainingLogRepository = nlpTrainingLogRepository;
    }

    @PostMapping(value = "/train", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> startTraining(
            @RequestParam(value = "file", required = false) MultipartFile trainFile,
            @RequestParam(value = "config", required = false) MultipartFile configFile,
            @RequestParam("learningRate") double learningRate,
            @RequestParam("epochs") int epochs,
            @RequestParam("batchSize") int batchSize,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (trainFile != null && !trainFile.isEmpty()) {
                saveToScriptsDir(trainFile, "train.py");
            }
            if (configFile != null && !configFile.isEmpty()) {
                String name = configFile.getOriginalFilename();
                if (name != null) {
                    saveToScriptsDir(configFile, name);
                }
            }
            Map<String, Object> hyperparameters = Map.of(
                    "learningRate", learningRate,
                    "epochs", epochs,
                    "batchSize", batchSize
            );
            Long jobId = nlpService.startTraining(hyperparameters, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("jobId", jobId));
        } catch (Exception e) {
            log.error("Training request failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> startTesting(
            @RequestParam(value = "file", required = false) MultipartFile testFile,
            @RequestParam(value = "model", required = false) MultipartFile modelFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (testFile != null && !testFile.isEmpty()) {
                saveToScriptsDir(testFile, "test.py");
            }
            if (modelFile != null && !modelFile.isEmpty()) {
                String name = modelFile.getOriginalFilename();
                if (name != null) {
                    saveToScriptsDir(modelFile, name);
                }
            }
            Long jobId = nlpService.startTesting(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("jobId", jobId));
        } catch (Exception e) {
            log.error("Test request failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void saveToScriptsDir(MultipartFile file, String filename) throws IOException {
        File dest = new File(scriptsDir, filename);
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create directory: " + parent.getAbsolutePath());
        }
        file.transferTo(dest);
        log.info("Saved uploaded file to {}", dest.getAbsolutePath());
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NlpLogResponse>> getTrainingLogs() {
        return ResponseEntity.ok(nlpService.getTrainingLogs());
    }

    @PostMapping("/metrics")
    public ResponseEntity<Void> receiveMetric(@RequestBody EpochMetricRequest request) {
        if (request.getJobId() == null || request.getEpoch() == null) {
            return ResponseEntity.badRequest().build();
        }
        TrainingMetric metric = TrainingMetric.builder()
                .jobId(request.getJobId())
                .epoch(request.getEpoch())
                .loss(request.getLoss())
                .accuracy(request.getAccuracy())
                .evalLoss(request.getEvalLoss())
                .evalAccuracy(request.getEvalAccuracy())
                .build();
        trainingMetricRepository.save(metric);

        Map<String, Object> data = Map.of(
                "jobId", request.getJobId(),
                "epoch", request.getEpoch(),
                "loss", request.getLoss() != null ? request.getLoss() : "",
                "accuracy", request.getAccuracy() != null ? request.getAccuracy() : "",
                "evalLoss", request.getEvalLoss() != null ? request.getEvalLoss() : "",
                "evalAccuracy", request.getEvalAccuracy() != null ? request.getEvalAccuracy() : ""
        );
        metricsSseService.broadcast(data);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/metrics/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMetrics() {
        return metricsSseService.createEmitter();
    }

    @GetMapping("/metrics/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TrainingMetric>> getJobMetrics(@PathVariable Long jobId) {
        return ResponseEntity.ok(trainingMetricRepository.findByJobIdOrderByEpochAsc(jobId));
    }

    @GetMapping("/models/{logId}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadModel(@PathVariable Long logId) {
        Optional<NlpTrainingLog> opt = nlpTrainingLogRepository.findById(logId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        NlpTrainingLog trainingLog = opt.get();
        if (trainingLog.getMetrics() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            tools.jackson.databind.JsonNode root = new tools.jackson.databind.ObjectMapper().readTree(trainingLog.getMetrics());
            String modelPathStr = root.has("modelPath") ? root.get("modelPath").asText() : null;
            if (modelPathStr == null || modelPathStr.isBlank()) {
                modelPathStr = modelsDir + File.separator + "job_" + trainingLog.getId() + ".pt";
            }
            File modelFile = new File(modelPathStr);
            if (!modelFile.exists()) {
                modelFile = new File(modelsDir, "job_" + trainingLog.getId() + ".pt");
            }
            if (!modelFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(modelFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + modelFile.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to serve model for log {}", logId, e);
            return ResponseEntity.notFound().build();
        }
    }
}
