package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Many-to-many join between {@link User} (annotator) and {@link Dataset}.
 *
 * <p>Modelled as an explicit entity (rather than a plain {@code @ManyToMany})
 * so we can store the assignment timestamp and track progress per annotator
 * per dataset.</p>
 *
 * <p>The service layer populates this table when an admin calls
 * {@code POST /api/admin/datasets/{id}/assign}.</p>
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_assignment_dataset_annotator",
                        columnNames = {"dataset_id", "annotator_id"}
                )
        },
        indexes = {
                @Index(name = "idx_assignments_dataset_id",   columnList = "dataset_id"),
                @Index(name = "idx_assignments_annotator_id", columnList = "annotator_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annotator_id", nullable = false)
    private User annotator;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

}