package com.ensah.nlp_annotation_platform.service.nlp;

import com.ensah.nlp_annotation_platform.domain.NlpTrainingLog;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.repository.NlpTrainingLogRepository;
import com.ensah.nlp_annotation_platform.repository.UserRepository;
import com.ensah.nlp_annotation_platform.service.job.JobService;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Service
@Transactional
public class NlpAsyncExecutor {

    private static final Logger log = LoggerFactory.getLogger(NlpAsyncExecutor.class);

    private final JobService jobService;
    private final NlpTrainingLogRepository trainingLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Semaphore nlpSemaphore;

    @Value("${nlp.python.executable:python}")
    private String pythonExecutable;

    @Value("${nlp.scripts.dir:python}")
    private String scriptsDir;

    public NlpAsyncExecutor(JobService jobService,
                            NlpTrainingLogRepository trainingLogRepository,
                            UserRepository userRepository,
                            ObjectMapper objectMapper,
                            @Value("${nlp.max-concurrent-jobs:2}") int maxConcurrentJobs) {
        this.jobService = jobService;
        this.trainingLogRepository = trainingLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.nlpSemaphore = new Semaphore(maxConcurrentJobs);
    }

    @Async("taskExecutor")
    public void executeTrainingAsync(Long jobId, Long userId, Map<String, Object> hyperparameters) {
        try {
            nlpSemaphore.acquire();
            jobService.updateProgress(jobId, 5);

            String scriptPath = scriptsDir + File.separator + "train.py";
            String hyperJson = objectMapper.writeValueAsString(hyperparameters);

            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, scriptPath, hyperJson);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    log.debug("[train.py] {}", line);
                }
            }

            int exitCode = process.waitFor();
            String logs = output.toString();

            if (exitCode == 0) {
                String result = extractLastJsonLine(logs);
                jobService.completeSuccess(jobId, result, logs);
                saveTrainingLog(userId, hyperparameters, result, NlpTrainingLog.Status.SUCCESS, logs);
            } else {
                jobService.completeFailed(jobId, "Python script exited with code " + exitCode, logs);
                saveTrainingLog(userId, hyperparameters, null, NlpTrainingLog.Status.FAILED, logs);
            }
        } catch (Exception e) {
            log.error("Training job {} failed", jobId, e);
            jobService.completeFailed(jobId, e.getMessage(), e.getMessage());
            saveTrainingLog(userId, hyperparameters, null, NlpTrainingLog.Status.FAILED, e.getMessage());
        } finally {
            nlpSemaphore.release();
        }
    }

    @Async("taskExecutor")
    public void executeTestingAsync(Long jobId, Long userId) {
        try {
            nlpSemaphore.acquire();
            jobService.updateProgress(jobId, 5);

            String scriptPath = scriptsDir + File.separator + "test.py";
            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, scriptPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    log.debug("[test.py] {}", line);
                }
            }

            int exitCode = process.waitFor();
            String logs = output.toString();

            if (exitCode == 0) {
                String result = extractLastJsonLine(logs);
                jobService.completeSuccess(jobId, result, logs);
                saveTrainingLog(userId, null, result, NlpTrainingLog.Status.SUCCESS, logs);
            } else {
                jobService.completeFailed(jobId, "Python script exited with code " + exitCode, logs);
                saveTrainingLog(userId, null, null, NlpTrainingLog.Status.FAILED, logs);
            }
        } catch (Exception e) {
            log.error("Test job {} failed", jobId, e);
            jobService.completeFailed(jobId, e.getMessage(), e.getMessage());
            saveTrainingLog(userId, null, null, NlpTrainingLog.Status.FAILED, e.getMessage());
        } finally {
            nlpSemaphore.release();
        }
    }

    private void saveTrainingLog(Long userId, Map<String, Object> hyperparameters, String metricsJson,
                                 NlpTrainingLog.Status status, String logs) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.error("Cannot save training log: user {} not found", userId);
                return;
            }

            NlpTrainingLog trainingLog = new NlpTrainingLog();
            trainingLog.setTriggeredBy(user);
            trainingLog.setHyperparameters(toJson(hyperparameters));
            trainingLog.setMetrics(metricsJson);
            trainingLog.setStatus(status);
            trainingLog.setExecutionLogs(logs);
            if (status == NlpTrainingLog.Status.SUCCESS || status == NlpTrainingLog.Status.FAILED) {
                trainingLog.setCompletedAt(Instant.now());
            }
            trainingLogRepository.save(trainingLog);
        } catch (Exception e) {
            log.error("Failed to save training log for user {}", userId, e);
        }
    }

    private String extractLastJsonLine(String output) {
        String[] lines = output.split("\\r?\\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.startsWith("{") && line.endsWith("}")) {
                return line;
            }
        }
        return output;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON", e);
            return null;
        }
    }
}
