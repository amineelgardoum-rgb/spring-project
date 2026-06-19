package com.ensah.nlp_annotation_platform.service.admin.impl;

import com.ensah.nlp_annotation_platform.domain.Annotation;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.response.admin.AnnotatorProgressEntry;
import com.ensah.nlp_annotation_platform.dto.response.admin.DashboardStatsResponse;
import com.ensah.nlp_annotation_platform.dto.response.admin.SpammerInfo;
import com.ensah.nlp_annotation_platform.repository.*;
import com.ensah.nlp_annotation_platform.service.admin.AdminDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final DatasetRepository datasetRepository;
    private final TextItemRepository textItemRepository;
    private final AnnotationRepository annotationRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    public AdminDashboardServiceImpl(DatasetRepository datasetRepository,
                                     TextItemRepository textItemRepository,
                                     AnnotationRepository annotationRepository,
                                     UserRepository userRepository,
                                     AssignmentRepository assignmentRepository) {
        this.datasetRepository = datasetRepository;
        this.textItemRepository = textItemRepository;
        this.annotationRepository = annotationRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public DashboardStatsResponse getStats() {
        long totalDatasets = datasetRepository.count();
        long totalTexts = textItemRepository.count();
        long totalAssignments = assignmentRepository.count();

        List<Long> assignedAnnotatorIds = assignmentRepository.findAll().stream()
                .map(a -> a.getAnnotator().getId())
                .distinct()
                .toList();
        long totalAnnotators = assignedAnnotatorIds.size();

        List<Annotation> allAnnotations = annotationRepository.findAll();
        long totalAnnotations = allAnnotations.size();

        double overallAnnotationPercent = 0.0;
        if (totalTexts > 0 && totalAnnotators > 0) {
            overallAnnotationPercent = (double) totalAnnotations / (totalTexts * totalAnnotators) * 100;
        }

        Map<String, Long> globalClassDistribution = allAnnotations.stream()
                .collect(Collectors.groupingBy(Annotation::getLabel, Collectors.counting()));

        List<User> annotators = userRepository.findAllById(assignedAnnotatorIds).stream()
                .filter(u -> !u.getDeleted())
                .toList();

        List<AnnotatorProgressEntry> annotatorProgress = new ArrayList<>();
        List<SpammerInfo> spammers = new ArrayList<>();

        for (User annotator : annotators) {
            List<Annotation> userAnnotations = annotationRepository.findByAnnotator_Id(annotator.getId());
            if (userAnnotations.isEmpty()) continue;

            long count = userAnnotations.size();
            Map<String, Long> classDist = userAnnotations.stream()
                    .collect(Collectors.groupingBy(Annotation::getLabel, Collectors.counting()));

            double avgTime = computeAvgTime(userAnnotations);

            annotatorProgress.add(AnnotatorProgressEntry.builder()
                    .annotatorId(annotator.getId())
                    .firstName(annotator.getFirstName())
                    .lastName(annotator.getLastName())
                    .annotatedCount(count)
                    .avgTimePerAnnotation(avgTime)
                    .classDistribution(classDist)
                    .build());

            if (isSpammer(classDist, count)) {
                spammers.add(SpammerInfo.builder()
                        .id(annotator.getId())
                        .firstName(annotator.getFirstName())
                        .lastName(annotator.getLastName())
                        .build());
            }
        }

        return DashboardStatsResponse.builder()
                .totalDatasets(totalDatasets)
                .totalTexts(totalTexts)
                .totalAnnotators(totalAnnotators)
                .totalAnnotations(totalAnnotations)
                .totalAssignments(totalAssignments)
                .overallAnnotationPercent(overallAnnotationPercent)
                .globalClassDistribution(globalClassDistribution)
                .annotatorProgress(annotatorProgress)
                .spammers(spammers)
                .build();
    }

    private double computeAvgTime(List<Annotation> annotations) {
        if (annotations.size() < 2) return 0.0;
        List<Annotation> sorted = annotations.stream()
                .sorted(Comparator.comparing(Annotation::getCreatedAt))
                .toList();
        long totalMillis = 0;
        int gaps = 0;
        for (int i = 1; i < sorted.size(); i++) {
            long diff = java.time.Duration.between(sorted.get(i - 1).getCreatedAt(), sorted.get(i).getCreatedAt()).toMillis();
            if (diff > 0 && diff < 300_000) {
                totalMillis += diff;
                gaps++;
            }
        }
        return gaps > 0 ? (double) totalMillis / gaps / 1000.0 : 0.0;
    }

    private boolean isSpammer(Map<String, Long> classDist, long total) {
        if (total == 0) return false;
        long maxCount = classDist.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return (double) maxCount / total >= 0.95;
    }
}
