package com.ensah.nlp_annotation_platform.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    @NotBlank(message = "username is required")
    private String username;
}
