package com.ensah.nlp_annotation_platform.service.job;

import com.ensah.nlp_annotation_platform.domain.Job;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.response.JobResponse;

import java.util.Optional;

public interface JobService {
    Job createJob(Job.JobType type, String hyperparameters, User triggeredBy);
    void updateProgress(Long jobId, int progress);
    void completeSuccess(Long jobId, String result, String logs);
    void completeFailed(Long jobId, String errorMessage, String logs);
    Optional<JobResponse> getJobById(Long id);
}
