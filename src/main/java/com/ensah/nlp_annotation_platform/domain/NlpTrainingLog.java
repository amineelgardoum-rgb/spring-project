package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Persistent record of every NLP training or evaluation run triggered from
 * the admin interface.
 *
 * <p>{@code hyperparameters} and {@code metrics} are stored as JSON strings
 * so the schema remains flexible as the Python scripts evolve.  Typical
 * contents:</p>
 * <ul>
 *   <li>{@code hyperparameters}: {@code {"epochs":10,"batchSize":32,"lr":1e-4}}</li>
 *   <li>{@code metrics}: {@code {"accuracy":0.91,"f1Score":0.89,"confusionMatrix":[[50,5],[3,42]]}}</li>
 * </ul>
 *
 * <p>{@code executionLogs} captures the combined stdout/stderr of the Python
 * process so the admin can diagnose failures without SSH access.</p>
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "nlp_training_logs",
        indexes = {
                @Index(name = "idx_nlp_logs_user_id",     columnList = "user_id"),
                @Index(name = "idx_nlp_logs_status",       columnList = "status"),
                @Index(name = "idx_nlp_logs_started_at",   columnList = "started_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NlpTrainingLog {

    // ------------------------------------------------------------------
    // Status lifecycle:  PENDING → RUNNING → SUCCESS | FAILED
    // ------------------------------------------------------------------
    public enum Status {PENDING, RUNNING, SUCCESS, FAILED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Admin who triggered the run.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User triggeredBy;

    /**
     * JSON object of training hyperparameters (epochs, batchSize, learningRate,
     * modelType, …).  Stored as TEXT so downstream script changes need no
     * schema migration.
     */
    @Column(columnDefinition = "JSON")
    private String hyperparameters;

    /**
     * JSON object of evaluation metrics populated once the run completes
     * (accuracy, f1Score, confusionMatrix, …).  Null while status is
     * PENDING / RUNNING.
     */
    @Column(columnDefinition = "JSON")
    private String metrics;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    /**
     * Combined stdout + stderr from the Python process.
     * Large field — use MEDIUMTEXT on MySQL/MariaDB via the columnDefinition.
     */
    @Column(name = "execution_logs", columnDefinition = "MEDIUMTEXT")
    private String executionLogs;

    @CreatedDate
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    /**
     * Set by the service layer once the process terminates.
     */
    @Column(name = "completed_at")
    private Instant completedAt;

}