package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A Dataset groups a collection of {@link TextItem}s that share the same
 * annotation task (e.g. textual similarity, NLI, sentiment analysis).
 *
 * <p>The {@code labels} element collection stores the valid annotation classes
 * for this dataset (e.g. ["similar","not_similar"]).  Storing them here
 * avoids a separate {@code Label} entity while keeping each dataset
 * independently configurable.</p>
 */
@Entity
@Table(
        name = "datasets",
        indexes = {
                @Index(name = "idx_datasets_created_by", columnList = "created_by"),
                @Index(name = "idx_datasets_created_at",  columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Server-side path of the uploaded source file (CSV / JSON).
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * Total number of text items in this dataset (denormalised for fast display).
     */
    @Column(name = "num_records")
    private Integer numRecords;

    /**
     * Admin who created the dataset.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Valid annotation labels for this dataset.
     * Stored in join-table {@code dataset_labels(dataset_id, label)}.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dataset_labels",
            joinColumns = @JoinColumn(name = "dataset_id"))
    @Column(name = "label", nullable = false, length = 100)
    @OrderColumn(name = "label_order")
    private List<String> labels = new ArrayList<>();

    /**
     * Bi-directional convenience — not always loaded.
     */
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TextItem> textItems = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}