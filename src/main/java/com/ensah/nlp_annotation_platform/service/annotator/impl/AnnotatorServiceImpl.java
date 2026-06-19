package com.ensah.nlp_annotation_platform.service.annotator.impl;

import com.ensah.nlp_annotation_platform.domain.*;
import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorStatsResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.AnnotatorTaskResponse;
import com.ensah.nlp_annotation_platform.dto.response.annotator.TextPairResponse;
import com.ensah.nlp_annotation_platform.exception.ResourceNotFoundException;
import com.ensah.nlp_annotation_platform.repository.*;
import com.ensah.nlp_annotation_platform.service.annotator.AnnotatorService;
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
    public Page<TextPairResponse> getTextPairs(Long assignmentId, String username, Pageable pageable) {
        User user = findUser(username);
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        Dataset dataset = assignment.getDataset();
        Page<TextItem> textItems = textItemRepository.findByDatasetId(dataset.getId(), pageable);
        List<TextPairResponse> responses = textItems.getContent().stream()
                .map(item -> toTextPairResponse(item, dataset.getLabels(), user.getId()))
                .toList();
        return new PageImpl<>(responses, pageable, textItems.getTotalElements());
    }

    @Override
    public void submitAnnotation(Long textItemId, String label, Double duration, String username) {
        User user = findUser(username);
        TextItem textItem = textItemRepository.findById(textItemId)
                .orElseThrow(() -> new ResourceNotFoundException("TextItem not found"));

        Dataset dataset = textItem.getDataset();
        if (!dataset.getLabels().contains(label)) {
            throw new IllegalArgumentException("Invalid label '" + label + "'. Valid labels: " + dataset.getLabels());
        }

        Optional<Annotation> existing = annotationRepository
                .findByTextItem_IdAndAnnotator_Id(textItemId, user.getId());

        if (existing.isPresent()) {
            Annotation ann = existing.get();
            ann.setLabel(label);
            if (duration != null) {
                ann.setDuration(duration);
            }
            annotationRepository.save(ann);
        } else {
            Annotation ann = new Annotation();
            ann.setTextItem(textItem);
            ann.setAnnotator(user);
            ann.setLabel(label);
            ann.setDuration(duration);
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

        double avgTime = annotations.stream()
                .map(Annotation::getDuration)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return AnnotatorStatsResponse.builder()
                .totalAnnotated(totalAnnotated)
                .avgTimePerAnnotation(Math.round(avgTime * 100.0) / 100.0)
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
                .id(assignment.getId())
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
