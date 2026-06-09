package com.ensah.nlp_annotation_platform.dto.request.admin;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String username;
}
