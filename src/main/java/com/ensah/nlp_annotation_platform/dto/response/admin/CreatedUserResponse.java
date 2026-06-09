package com.ensah.nlp_annotation_platform.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatedUserResponse {
    private Long id;
    private String username;
    private String password;
}
