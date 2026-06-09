package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Records a single annotator's label choice for a {@link TextItem}.
 *
 * <p>The unique constraint {@code (text_item_id, annotator_id)} ensures each
 * annotator can only submit one annotation per item — re-submitting triggers
 * an update rather than an insert (handled in the service layer with
 * {@code findByTextItemAndAnnotator}).</p>
 *
 * <p>An optional {@code comment} lets an admin correct a label inline
 * (soft-override) without removing the original record.</p>
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "annotations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name  = "uq_annotation_item_annotator",
                        columnNames = {"text_item_id", "annotator_id"}
                )
        },
        indexes = {
                @Index(name = "idx_annotations_text_item_id",  columnList = "text_item_id"),
                @Index(name = "idx_annotations_annotator_id",  columnList = "annotator_id"),
                @Index(name = "idx_annotations_created_at",    columnList = "created_at")
        }
)
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "text_item_id", nullable = false)
    private TextItem textItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annotator_id", nullable = false)
    private User annotator;

    /**
     * The chosen label (must belong to {@code textItem.dataset.labels}).
     * Validation happens in the service layer.
     */
    @Column(nullable = false, length = 100)
    private String label;

    /**
     * Optional free-text comment from the annotator or an admin correction note.
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}