package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "training_metrics", indexes = {
        @Index(name = "idx_training_metrics_job_id", columnList = "job_id"),
        @Index(name = "idx_training_metrics_epoch", columnList = "job_id,epoch")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private Integer epoch;

    @Column
    private Double loss;

    @Column
    private Double accuracy;

    @Column(name = "eval_loss")
    private Double evalLoss;

    @Column(name = "eval_accuracy")
    private Double evalAccuracy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
