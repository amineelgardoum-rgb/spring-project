package com.ensah.nlp_annotation_platform.service.annotator.impl;

import com.ensah.nlp_annotation_platform.domain.*;
import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorStatsResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorTaskResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.TextPairResponse;
import com.ensah.nlp_annotation_platform.exception.ResourceNotFoundException;
import com.ensah.nlp_annotation_platform.repository.*;
import com.ensah.nlp_annotation_platform.service.annotator.AnnotatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnotatorServiceImpl implements AnnotatorService {

    private static final Logger log = LoggerFactory.getLogger(AnnotatorServiceImpl.class);

    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final DatasetRepository datasetRepository;
    private final TextItemRepository textItemRepository;
    private final AnnotationRepository annotationRepository;

    public AnnotatorServiceImpl(UserRepository userRepository,
                                AssignmentRepository assignmentRepository,
                                DatasetRepository datasetRepository,
                                TextItemRepository textItemRepository,
                                AnnotationRepository annotationRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.datasetRepository = datasetRepository;
        this.textItemRepository = textItemRepository;
        this.annotationRepository = annotationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnotatorTaskResponse> getTasks(String username, Pageable pageable) {
        User user = findUser(username);
        Page<Assignment> assignments = assignmentRepository.findByAnnotator_Id(user.getId(), pageable);
        List<AnnotatorTaskResponse> responses = assignments.getContent().stream()
                .map(this::toTaskResponse)
                .toList();
        return new PageImpl<>(responses, pageable, assignments.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TextPairResponse> getTextPairs(Long datasetId, String username, Pageable pageable) {
        User user = findUser(username);
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));
        Page<TextItem> textItems = textItemRepository.findByDatasetId(datasetId, pageable);
        List<TextPairResponse> responses = textItems.getContent().stream()
                .map(item -> toTextPairResponse(item, dataset.getLabels(), user.getId()))
                .toList();
        return new PageImpl<>(responses, pageable, textItems.getTotalElements());
    }

    @Override
    public void submitAnnotation(Long datasetId, Long textItemId, String label, String username) {
        User user = findUser(username);
        TextItem textItem = textItemRepository.findById(textItemId)
                .orElseThrow(() -> new ResourceNotFoundException("TextItem not found"));

        if (!textItem.getDataset().getId().equals(datasetId)) {
            throw new IllegalArgumentException("TextItem does not belong to this dataset");
        }

        Dataset dataset = textItem.getDataset();
        if (!dataset.getLabels().contains(label)) {
            throw new IllegalArgumentException("Invalid label '" + label + "'. Valid labels: " + dataset.getLabels());
        }

        Optional<Annotation> existing = annotationRepository
                .findByTextItem_IdAndAnnotator_Id(textItemId, user.getId());

        if (existing.isPresent()) {
            Annotation ann = existing.get();
            ann.setLabel(label);
            annotationRepository.save(ann);
        } else {
            Annotation ann = new Annotation();
            ann.setTextItem(textItem);
            ann.setAnnotator(user);
            ann.setLabel(label);
            annotationRepository.save(ann);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AnnotatorStatsResponse getStats(String username) {
        User user = findUser(username);
        long totalAnnotated = annotationRepository.countByAnnotator_Id(user.getId());
        List<Annotation> annotations = annotationRepository.findByAnnotator_Id(user.getId());

        Map<String, Long> distribution = annotations.stream()
                .collect(Collectors.groupingBy(Annotation::getLabel, Collectors.counting()));

        return AnnotatorStatsResponse.builder()
                .totalAnnotated(totalAnnotated)
                .avgTimePerAnnotation(0.0)
                .classDistribution(distribution)
                .build();
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private AnnotatorTaskResponse toTaskResponse(Assignment assignment) {
        Dataset dataset = assignment.getDataset();
        long totalItems = textItemRepository.countByDatasetId(dataset.getId());
        long annotatedItems = annotationRepository
                .countByTextItem_Dataset_IdAndAnnotator_Id(dataset.getId(), assignment.getAnnotator().getId());
        double pct = totalItems > 0 ? (double) annotatedItems / totalItems * 100.0 : 0.0;

        return AnnotatorTaskResponse.builder()
                .datasetId(dataset.getId())
                .datasetName(dataset.getName())
                .datasetDescription(dataset.getDescription())
                .totalItems(totalItems)
                .annotatedItems(annotatedItems)
                .completionPercentage(Math.round(pct * 100.0) / 100.0)
                .assignedAt(assignment.getAssignedAt() != null
                        ? java.time.LocalDateTime.ofInstant(assignment.getAssignedAt(),
                                java.time.ZoneId.systemDefault())
                        : null)
                .build();
    }

    private TextPairResponse toTextPairResponse(TextItem item, List<String> labels, Long userId) {
        String currentLabel = null;
        String currentComment = null;
        Optional<Annotation> existing = annotationRepository
                .findByTextItem_IdAndAnnotator_Id(item.getId(), userId);
        if (existing.isPresent()) {
            currentLabel = existing.get().getLabel();
            currentComment = existing.get().getComment();
        }

        return TextPairResponse.builder()
                .textItemId(item.getId())
                .content(item.getContent())
                .pairContent(item.getPairContent())
                .metadata(item.getMetadata())
                .availableLabels(labels)
                .currentLabel(currentLabel)
                .currentComment(currentComment)
                .build();
    }
}
