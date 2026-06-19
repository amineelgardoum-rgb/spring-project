package com.ensah.nlp_annotation_platform.service.dataset.impl;

import com.ensah.nlp_annotation_platform.domain.*;
import com.ensah.nlp_annotation_platform.dto.response.dataset.DatasetDetailResponse;
import com.ensah.nlp_annotation_platform.dto.response.dataset.DatasetResponse;
import com.ensah.nlp_annotation_platform.exception.ResourceNotFoundException;
import com.ensah.nlp_annotation_platform.repository.AnnotationRepository;
import com.ensah.nlp_annotation_platform.repository.AssignmentRepository;
import com.ensah.nlp_annotation_platform.repository.DatasetRepository;
import com.ensah.nlp_annotation_platform.repository.TextItemRepository;
import com.ensah.nlp_annotation_platform.repository.UserRepository;
import com.ensah.nlp_annotation_platform.service.dataset.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;


@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetServiceImpl.class);

    private final DatasetRepository datasetRepository;
    private final TextItemRepository textItemRepository;
    private final AssignmentRepository assignmentRepository;
    private final AnnotationRepository annotationRepository;
    private final UserRepository userRepository;

    @Value("${app.admin.username}")
    private String adminUsername;

    public DatasetServiceImpl(DatasetRepository datasetRepository,
                              TextItemRepository textItemRepository,
                              AssignmentRepository assignmentRepository,
                              AnnotationRepository annotationRepository,
                              UserRepository userRepository) {
        this.datasetRepository = datasetRepository;
        this.textItemRepository = textItemRepository;
        this.assignmentRepository = assignmentRepository;
        this.annotationRepository = annotationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void uploadDataset(MultipartFile file, String tags, String name, String description) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is required");
        }

        List<String> labels = parseTags(tags);
        String datasetName = (name != null && !name.isBlank()) ? name
                : (filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename);
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found: " + adminUsername));

        Dataset dataset = new Dataset();
        dataset.setName(datasetName);
        dataset.setDescription(description != null && !description.isBlank() ? description : "Uploaded from " + filename);
        dataset.setFilePath("");
        dataset.setCreatedBy(admin);
        dataset.setLabels(labels);
        dataset.setCreatedAt(LocalDateTime.now());
        dataset.setUpdatedAt(LocalDateTime.now());
        dataset = datasetRepository.save(dataset);

        try {
            String content = new String(file.getBytes());
            String lowerName = filename.toLowerCase();
            List<TextItem> items;

            if (lowerName.endsWith(".json") || lowerName.endsWith(".jsonl")) {
                items = parseJsonLines(content, dataset);
            } else {
                items = parseCsv(content, dataset);
            }

            dataset.setNumRecords(items.size());
            datasetRepository.save(dataset);
            log.info("Uploaded dataset '{}' with {} items and {} labels", name, items.size(), labels);

        } catch (Exception e) {
            log.error("Failed to parse dataset file: {}", filename, e);
            datasetRepository.delete(dataset);
            throw new RuntimeException("Failed to parse file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatasetResponse> listDatasets() {
        return datasetRepository.findAll().stream()
                .map(this::toDatasetResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getDatasetDetail(Long id) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));

        List<Assignment> assignments = assignmentRepository.findByDataset_Id(id);
        List<Long> assignedIds = assignments.stream()
                .map(a -> a.getAnnotator().getId())
                .toList();

        List<DatasetDetailResponse.AnnotatorInfo> annotatorInfos = assignments.stream()
                .map(a -> DatasetDetailResponse.AnnotatorInfo.builder()
                        .id(a.getAnnotator().getId())
                        .username(a.getAnnotator().getUsername())
                        .email(a.getAnnotator().getUsername()) // using username as email fallback
                        .build())
                .toList();

        long totalItems = textItemRepository.countByDatasetId(id);
        long annotatedItems = annotationRepository.findByTextItem_Dataset_Id(id).stream()
                .map(a -> a.getTextItem().getId())
                .distinct()
                .count();
        double pct = totalItems > 0 ? (double) annotatedItems / totalItems * 100.0 : 0.0;
        double progress = Math.round(pct * 100.0) / 100.0;

        List<DatasetDetailResponse.TextItemInfo> textItemInfos = textItemRepository.findByDatasetId(id).stream()
                .map(item -> DatasetDetailResponse.TextItemInfo.builder()
                        .id(item.getId())
                        .sourceText(item.getContent())
                        .targetText(item.getPairContent())
                        .build())
                .toList();

        return com.ensah.nlp_annotation_platform.dto.response.dataset.DatasetDetailResponse.builder()
                .id(dataset.getId())
                .name(dataset.getName())
                .description(dataset.getDescription())
                .filePath(dataset.getFilePath())
                .numRecords(dataset.getNumRecords())
                .createdBy(dataset.getCreatedBy().getUsername())
                .labels(dataset.getLabels())
                .assignedAnnotatorIds(assignedIds)
                .progress(progress)
                .annotators(annotatorInfos)
                .textItems(textItemInfos)
                .createdAt(dataset.getCreatedAt())
                .updatedAt(dataset.getUpdatedAt())
                .build();
    }

    @Override
    public void assignAnnotators(Long id, List<Long> annotatorIds) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found"));

        for (Long annotatorId : annotatorIds) {
            User annotator = userRepository.findById(annotatorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Annotator not found: " + annotatorId));

            Optional<Assignment> existing = assignmentRepository
                    .findByDataset_IdAndAnnotator_Id(id, annotatorId);
            if (existing.isEmpty()) {
                Assignment assignment = new Assignment();
                assignment.setDataset(dataset);
                assignment.setAnnotator(annotator);
                assignmentRepository.save(assignment);
            }
        }
    }

    @Override
    public void removeAnnotator(Long id, Long userId) {
        assignmentRepository.findByDataset_IdAndAnnotator_Id(id, userId)
                .ifPresent(assignmentRepository::delete);
    }

    private DatasetResponse toDatasetResponse(Dataset dataset) {
        DatasetResponse dto = new DatasetResponse();
        dto.setId(dataset.getId());
        dto.setName(dataset.getName());

        long totalItems = textItemRepository.countByDatasetId(dataset.getId());
        long totalAnnotations = annotationRepository.findByTextItem_Dataset_Id(dataset.getId()).size();
        dto.setTotalAnnotations(totalAnnotations);
        long uniqueAnnotated = annotationRepository.findByTextItem_Dataset_Id(dataset.getId()).stream()
                .map(a -> a.getTextItem().getId())
                .distinct()
                .count();
        double pct = totalItems > 0 ? (double) uniqueAnnotated / totalItems * 100.0 : 0.0;
        dto.setCompletionPercentage(Math.round(pct * 100.0) / 100.0);
        return dto;
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) return List.of();
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private List<TextItem> parseCsv(String content, Dataset dataset) {
        List<TextItem> items = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");
        if (lines.length < 2) return items;

        String[] headers = parseCsvLine(lines[0]);

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] values = parseCsvLine(line);
            if (values.length == 0) continue;

            TextItem item = new TextItem();
            item.setDataset(dataset);
            item.setContent(values[0]);
            if (values.length > 1) {
                item.setPairContent(values[1]);
            }

            Map<String, String> meta = new LinkedHashMap<>();
            for (int j = 2; j < values.length && j < headers.length; j++) {
                meta.put(headers[j], values[j]);
            }
            if (!meta.isEmpty()) {
                try {
                    item.setMetadata(new tools.jackson.databind.ObjectMapper().writeValueAsString(meta));
                } catch (Exception e) {
                    log.warn("Failed to serialize metadata", e);
                }
            }

            items.add(item);
        }

        return textItemRepository.saveAll(items);
    }

    @SuppressWarnings("unchecked")
    private List<TextItem> parseJsonLines(String content, Dataset dataset) {
        List<TextItem> items = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                Map<String, Object> map = mapper.readValue(line, Map.class);
                TextItem item = new TextItem();
                item.setDataset(dataset);
                item.setContent(String.valueOf(map.getOrDefault("text", map.getOrDefault("content", ""))));
                if (map.containsKey("pair")) {
                    item.setPairContent(String.valueOf(map.get("pair")));
                }

                Map<String, Object> meta = new LinkedHashMap<>(map);
                meta.remove("text");
                meta.remove("content");
                meta.remove("pair");
                meta.remove("label");
                if (!meta.isEmpty()) {
                    item.setMetadata(mapper.writeValueAsString(meta));
                }

                items.add(item);
            } catch (Exception e) {
                log.warn("Failed to parse JSON line: {}", line, e);
            }
        }

        return textItemRepository.saveAll(items);
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }
}
