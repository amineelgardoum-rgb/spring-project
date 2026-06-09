package com.ensah.nlp_annotation_platform.repository;

import com.ensah.nlp_annotation_platform.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(Job.JobStatus status);
}
