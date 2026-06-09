package com.ensah.nlp_annotation_platform.service.job.impl;

import com.ensah.nlp_annotation_platform.domain.Job;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.response.JobResponse;
import com.ensah.nlp_annotation_platform.exception.ResourceNotFoundException;
import com.ensah.nlp_annotation_platform.repository.JobRepository;
import com.ensah.nlp_annotation_platform.service.job.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public Job createJob(Job.JobType type, String hyperparameters, User triggeredBy) {
        Job job = new Job();
        job.setType(type);
        job.setStatus(Job.JobStatus.PENDING);
        job.setProgress(0);
        job.setTriggeredBy(triggeredBy);
        job.setHyperparameters(hyperparameters);
        return jobRepository.save(job);
    }

    @Override
    public void updateProgress(Long jobId, int progress) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setProgress(progress);
        if (job.getStatus() == Job.JobStatus.PENDING) {
            job.setStatus(Job.JobStatus.RUNNING);
        }
        jobRepository.save(job);
    }

    @Override
    public void completeSuccess(Long jobId, String result, String logs) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setStatus(Job.JobStatus.SUCCESS);
        job.setProgress(100);
        job.setResult(result);
        job.setExecutionLogs(logs);
        job.setCompletedAt(Instant.now());
        jobRepository.save(job);
    }

    @Override
    public void completeFailed(Long jobId, String errorMessage, String logs) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setStatus(Job.JobStatus.FAILED);
        job.setErrorMessage(errorMessage);
        job.setExecutionLogs(logs);
        job.setCompletedAt(Instant.now());
        jobRepository.save(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobResponse> getJobById(Long id) {
        return jobRepository.findById(id).map(this::toResponse);
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .type(job.getType().name())
                .status(job.getStatus().name())
                .progress(job.getProgress())
                .triggeredById(job.getTriggeredBy().getId())
                .triggeredByUsername(job.getTriggeredBy().getUsername())
                .hyperparameters(job.getHyperparameters())
                .result(job.getResult())
                .executionLogs(job.getExecutionLogs())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}
