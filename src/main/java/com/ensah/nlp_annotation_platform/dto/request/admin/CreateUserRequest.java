package com.ensah.nlp_annotation_platform.dto.request.admin;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String username;
}
