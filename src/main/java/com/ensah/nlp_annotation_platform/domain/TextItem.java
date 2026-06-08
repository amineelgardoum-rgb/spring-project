package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A single annotatable unit inside a {@link Dataset}.
 *
 * <p>Supports both single-text tasks (sentiment analysis) and paired-text
 * tasks (NLI, textual similarity) via the nullable {@code pairContent}
 * field.  Extra source-file columns are preserved in {@code metadata} as
 * a JSON string so no information is lost during import.</p>
 *
 * <p>The {@code @Version} field enables optimistic locking: if two
 * annotators save an annotation simultaneously, only one will succeed and
 * the other will receive an {@code OptimisticLockException} that the
 * service layer can retry gracefully.</p>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "text_items",
        indexes = {
                @Index(name = "idx_text_items_dataset_id",  columnList = "dataset_id"),
                @Index(name = "idx_text_items_created_at",  columnList = "created_at")
        }
)
public class TextItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    /** Primary text (always present). */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Second text in a pair (nullable — only for paired-text tasks like NLI
     * or textual similarity).
     */
    @Column(name = "pair_content", columnDefinition = "TEXT")
    private String pairContent;

    /**
     * Additional key-value metadata from the original CSV/JSON file stored as
     * a JSON object string.  Avoids losing source columns not directly modelled.
     * Example: {@code {"source": "wikipedia", "original_id": "123"}}.
     */
    @Column(columnDefinition = "JSON")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Optimistic locking version — incremented on every flush.
     * Prevents two concurrent annotation saves from creating a lost update.
     */
    @Version
    private Long version;

    @OneToMany(mappedBy = "textItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Annotation> annotations = new ArrayList<>();

}