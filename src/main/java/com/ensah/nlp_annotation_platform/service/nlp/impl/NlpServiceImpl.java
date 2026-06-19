package com.ensah.nlp_annotation_platform.service.nlp.impl;

import com.ensah.nlp_annotation_platform.domain.Job;
import com.ensah.nlp_annotation_platform.domain.NlpTrainingLog;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.response.NlpLogResponse;
import com.ensah.nlp_annotation_platform.repository.NlpTrainingLogRepository;
import com.ensah.nlp_annotation_platform.repository.UserRepository;
import com.ensah.nlp_annotation_platform.service.job.JobService;
import com.ensah.nlp_annotation_platform.service.nlp.NlpAsyncExecutor;
import com.ensah.nlp_annotation_platform.service.nlp.NlpService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NlpServiceImpl implements NlpService {

    private static final Logger logger = LoggerFactory.getLogger(NlpServiceImpl.class);

    private final NlpTrainingLogRepository trainingLogRepository;
    private final UserRepository userRepository;
    private final JobService jobService;
    private final NlpAsyncExecutor nlpAsyncExecutor;
    private final ObjectMapper objectMapper;

    public NlpServiceImpl(NlpTrainingLogRepository trainingLogRepository,
                          UserRepository userRepository,
                          JobService jobService,
                          NlpAsyncExecutor nlpAsyncExecutor,
                          ObjectMapper objectMapper) {
        this.trainingLogRepository = trainingLogRepository;
        this.userRepository = userRepository;
        this.jobService = jobService;
        this.nlpAsyncExecutor = nlpAsyncExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public Long startTraining(Map<String, Object> hyperparameters, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        String hyperJson = toJson(hyperparameters);
        Job job = jobService.createJob(Job.JobType.TRAIN, hyperJson, user);
        nlpAsyncExecutor.executeTrainingAsync(job.getId(), user.getId(), hyperparameters);
        return job.getId();
    }

    @Override
    public Long startTesting(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Job job = jobService.createJob(Job.JobType.TEST, null, user);
        nlpAsyncExecutor.executeTestingAsync(job.getId(), user.getId());
        return job.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NlpLogResponse> getTrainingLogs() {
        return trainingLogRepository.findAllByOrderByStartedAtDesc().stream()
                .map(this::toLogResponse)
                .toList();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed to serialize to JSON", e);
            return null;
        }
    }

    private NlpLogResponse toLogResponse(NlpTrainingLog log) {
        Double accuracy = null;
        Double f1Score = null;
        Double loss = null;
        String modelPath = null;
        if (log.getMetrics() != null) {
            try {
                JsonNode root = objectMapper.readTree(log.getMetrics());
                if (root.has("accuracy")) accuracy = root.get("accuracy").asDouble();
                if (root.has("f1Score")) f1Score = root.get("f1Score").asDouble();
                if (root.has("f1")) f1Score = root.get("f1").asDouble();
                if (root.has("loss")) loss = root.get("loss").asDouble();
                if (root.has("modelPath")) modelPath = root.get("modelPath").asText();
            } catch (Exception e) {
                logger.warn("Failed to parse metrics JSON for log {}", log.getId(), e);
            }
        }
        return NlpLogResponse.builder()
                .id(log.getId())
                .startedAt(log.getStartedAt())
                .completedAt(log.getCompletedAt())
                .triggeredById(log.getTriggeredBy().getId())
                .triggeredByUsername(log.getTriggeredBy().getUsername())
                .hyperparameters(log.getHyperparameters())
                .metrics(log.getMetrics())
                .accuracy(accuracy)
                .f1Score(f1Score)
                .loss(loss)
                .modelPath(modelPath)
                .status(log.getStatus().name())
                .executionLogs(log.getExecutionLogs())
                .build();
    }
}
