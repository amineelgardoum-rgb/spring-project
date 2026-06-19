package com.ensah.nlp_annotation_platform.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpammerInfo {
    private Long id;
    private String firstName;
    private String lastName;
}
