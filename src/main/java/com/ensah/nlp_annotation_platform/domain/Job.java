package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "jobs", indexes = {
        @Index(name = "idx_jobs_status", columnList = "status"),
        @Index(name = "idx_jobs_user_id", columnList = "user_id"),
        @Index(name = "idx_jobs_type", columnList = "type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    public enum JobType { TRAIN, TEST }

    public enum JobStatus { PENDING, RUNNING, SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.PENDING;

    @Column(nullable = false)
    private Integer progress = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User triggeredBy;

    @Column(columnDefinition = "JSON")
    private String hyperparameters;

    @Column(columnDefinition = "JSON")
    private String result;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String executionLogs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
