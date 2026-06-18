package com.ensah.nlp_annotation_platform.service.metrics.impl;

import com.ensah.nlp_annotation_platform.domain.Annotation;
import com.ensah.nlp_annotation_platform.domain.Dataset;
import com.ensah.nlp_annotation_platform.domain.TextItem;
import com.ensah.nlp_annotation_platform.dto.response.metrics.MetricsResponse;
import com.ensah.nlp_annotation_platform.exception.ResourceNotFoundException;
import com.ensah.nlp_annotation_platform.repository.*;
import com.ensah.nlp_annotation_platform.service.metrics.MetricsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MetricsServiceImpl implements MetricsService {

    private final DatasetRepository datasetRepository;
    private final TextItemRepository textItemRepository;
    private final AnnotationRepository annotationRepository;
    private final AssignmentRepository assignmentRepository;

    public MetricsServiceImpl(DatasetRepository datasetRepository,
                              TextItemRepository textItemRepository,
                              AnnotationRepository annotationRepository,
                              AssignmentRepository assignmentRepository) {
        this.datasetRepository = datasetRepository;
        this.textItemRepository = textItemRepository;
        this.annotationRepository = annotationRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public Object computeMetrics(Long datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));

        long totalItems = textItemRepository.countByDatasetId(datasetId);
        List<Annotation> annotations = annotationRepository.findByTextItem_Dataset_Id(datasetId);
        long totalAnnotations = annotations.size();
        long assignedAnnotators = assignmentRepository.findByDataset_Id(datasetId).size();

        Map<String, Long> overallDistribution = annotations.stream()
                .collect(Collectors.groupingBy(Annotation::getLabel, Collectors.counting()));

        Map<Long, Long> perAnnotator = annotations.stream()
                .collect(Collectors.groupingBy(a -> a.getAnnotator().getId(), Collectors.counting()));

        Double fleissKappa = totalItems > 0 && assignedAnnotators >= 2
                ? computeFleissKappa(datasetId, dataset.getLabels())
                : null;

        return MetricsResponse.builder()
                .totalItems(totalItems)
                .totalAnnotations(totalAnnotations)
                .assignedAnnotators(assignedAnnotators)
                .overallClassDistribution(overallDistribution)
                .fleissKappa(fleissKappa)
                .annotationsPerAnnotator(perAnnotator)
                .build();
    }

    @Override
    public ResponseEntity<StreamingResponseBody> exportDataset(Long datasetId, String format) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));

        List<TextItem> textItems = textItemRepository.findByDatasetId(datasetId);
        List<Annotation> annotations = annotationRepository.findByTextItem_Dataset_Id(datasetId);

        Map<Long, List<Annotation>> annotationMap = annotations.stream()
                .collect(Collectors.groupingBy(a -> a.getTextItem().getId()));

        StreamingResponseBody stream = out -> {
            if ("json".equalsIgnoreCase(format)) {
                exportAsJson(out, textItems, annotationMap);
            } else {
                exportAsCsv(out, textItems, annotationMap, dataset.getLabels());
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=dataset_" + datasetId + "." + format);
        headers.add(HttpHeaders.CONTENT_TYPE,
                "json".equalsIgnoreCase(format) ? "application/json" : "text/csv");

        return ResponseEntity.ok().headers(headers).body(stream);
    }

    private Double computeFleissKappa(Long datasetId, List<String> labels) {
        List<TextItem> items = textItemRepository.findByDatasetId(datasetId);
        if (items.isEmpty()) return null;

        List<Annotation> allAnnotations = annotationRepository.findByTextItem_Dataset_Id(datasetId);

        Map<Long, Map<String, Long>> itemLabelCounts = new HashMap<>();
        for (Annotation ann : allAnnotations) {
            itemLabelCounts.computeIfAbsent(ann.getTextItem().getId(), k -> new HashMap<>());
            Map<String, Long> counts = itemLabelCounts.get(ann.getTextItem().getId());
            counts.merge(ann.getLabel(), 1L, (a, b) -> a + b);
        }

        int k = labels.size();
        if (k < 2) return null;

        Set<Long> itemIdsWithAnnotations = itemLabelCounts.keySet();
        if (itemIdsWithAnnotations.isEmpty()) return null;

        int N = itemIdsWithAnnotations.size();
        int n = -1;
        for (Long itemId : itemIdsWithAnnotations) {
            Map<String, Long> counts = itemLabelCounts.get(itemId);
            int totalForItem = counts.values().stream().mapToInt(Long::intValue).sum();
            if (n == -1) {
                n = totalForItem;
            } else if (n != totalForItem) {
                n = Math.min(n, totalForItem);
            }
        }
        if (n < 2) return null;

        double[] pj = new double[k];
        double[] Pi = new double[N];

        int idx = 0;
        for (Long itemId : itemIdsWithAnnotations) {
            Map<String, Long> counts = itemLabelCounts.get(itemId);
            int totalForItem = counts.values().stream().mapToInt(Long::intValue).sum();
            if (totalForItem != n) continue;

            double sumSquares = 0;
            for (int j = 0; j < k; j++) {
                long count = counts.getOrDefault(labels.get(j), 0L);
                pj[j] += (double) count / (N * n);
                sumSquares += count * count;
            }
            Pi[idx] = (sumSquares - n) / (double) (n * (n - 1));
            idx++;
        }

        if (idx == 0) return null;

        double Pbar = Arrays.stream(Pi).limit(idx).average().orElse(0);
        double PbarE = Arrays.stream(pj).map(p -> p * p).sum();

        if (Math.abs(1 - PbarE) < 1e-10) return 1.0;

        return (Pbar - PbarE) / (1 - PbarE);
    }

    private void exportAsJson(java.io.OutputStream out,
                               List<TextItem> items,
                               Map<Long, List<Annotation>> annotationMap) throws java.io.IOException {
        List<Map<String, Object>> export = new ArrayList<>();
        for (TextItem item : items) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("textItemId", item.getId());
            entry.put("content", item.getContent());
            entry.put("pairContent", item.getPairContent());
            entry.put("annotations", annotationMap.getOrDefault(item.getId(), List.of()).stream()
                    .map(a -> Map.of(
                            "annotatorId", a.getAnnotator().getId(),
                            "annotator", a.getAnnotator().getUsername(),
                            "label", a.getLabel(),
                            "comment", a.getComment()
                    ))
                    .toList());
            export.add(entry);
        }
        new tools.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(out, export);
    }

    private void exportAsCsv(java.io.OutputStream out,
                              List<TextItem> items,
                              Map<Long, List<Annotation>> annotationMap,
                              List<String> labels) throws java.io.IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("textItemId,content,pairContent");
        for (String label : labels) {
            sb.append(",").append("count_").append(label);
        }
        sb.append(",annotators\n");

        for (TextItem item : items) {
            sb.append(item.getId()).append(",");
            sb.append(escapeCsv(item.getContent())).append(",");
            sb.append(escapeCsv(item.getPairContent()));

            List<Annotation> anns = annotationMap.getOrDefault(item.getId(), List.of());
            Map<String, Long> labelCounts = anns.stream()
                    .collect(Collectors.groupingBy(Annotation::getLabel, Collectors.counting()));
            for (String label : labels) {
                sb.append(",").append(labelCounts.getOrDefault(label, 0L));
            }

            String annotators = anns.stream()
                    .map(a -> a.getAnnotator().getUsername() + ":" + a.getLabel())
                    .collect(Collectors.joining(";"));
            sb.append(",").append(escapeCsv(annotators)).append("\n");
        }

        out.write(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
